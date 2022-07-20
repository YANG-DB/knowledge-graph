package org.opensearch.graph.executor.opensearch.logging;

/*-
 * #%L
 * virtual-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.opensearch.graph.dispatcher.logging.LogMessage;
import org.slf4j.MDC;

public class NetworkElasticElapsed {
    public static StartWriter start() {
        return new StartWriter(System.currentTimeMillis());
    }

    public static SingleWriter stop() {
        return new SingleWriter();
    }

    public static TotalWriter stopTotal() {
        return new TotalWriter();
    }

    public static class StartWriter implements LogMessage.MDCWriter {
        //region Static
        public static final String key = "elasticOperationStart";
        //endregion

        //region Constructors
        public StartWriter(long fromEpoch) {
            this.fromEpoch = fromEpoch;
        }
        //endregion

        //region LogMessage.MDCWriter Implementation
        @Override
        public void write() {
            MDC.put(key, Long.toString(this.fromEpoch));
        }
        //endregion

        //region Fields
        private long fromEpoch;
        //endregion
    }

    public static class SingleWriter implements LogMessage.MDCWriter {
        //region MDCWriter Implementation
        @Override
        public void write() {
            String startFromEpochString = MDC.get(StartWriter.key);
            if (startFromEpochString == null) {
                return;
            }

            long elapsed = System.currentTimeMillis() - Long.parseLong(startFromEpochString);
            MDC.put(SingleConverter.key, Long.toString(elapsed));
        }
        //endregion
    }


    public static class TotalWriter implements LogMessage.MDCWriter {
        //region MDCWriter Implementation
        @Override
        public void write() {
            String startFromEpochString = MDC.get(StartWriter.key);
            if (startFromEpochString == null) {
                return;
            }

            long elapsed = System.currentTimeMillis() - Long.parseLong(startFromEpochString);

            String elasticElapsedTotalString = MDC.get(TotalConverter.key);
            long elasticElapsedTotalCurrent = elasticElapsedTotalString == null ? 0 : Long.parseLong(elasticElapsedTotalString);


            MDC.put(TotalConverter.key, Long.toString(elasticElapsedTotalCurrent + elapsed));
        }
        //endregion
    }

    public static class SingleConverter extends ClassicConverter {
        public static final String key = "networkElasticElapsed";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }

    public static class TotalConverter extends ClassicConverter {
        public static final String key = "networkElasticElapsedTotal";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "0");
        }
        //endregion
    }
}
