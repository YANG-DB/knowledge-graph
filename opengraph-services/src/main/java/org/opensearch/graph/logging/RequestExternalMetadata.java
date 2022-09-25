package org.opensearch.graph.logging;

/*-
 * #%L
 * opengraph-services
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
import org.opensearch.graph.model.transport.ExternalMetadata;

public class RequestExternalMetadata {
    public static LogMessage.MDCWriter of(ExternalMetadata externalMetadata) {
        return LogMessage.MDCWriter.Composite.of(
                new LogMessage.MDCWriter.KeyValue(RequestExternalMetadata.IdConverter.key, externalMetadata.getId()),
                new LogMessage.MDCWriter.KeyValue(RequestExternalMetadata.OperationConverter.key, externalMetadata.getOperation())
        );
    }

    public static class IdConverter extends ClassicConverter {
        public static final String key = "requestExternalId";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }

    public static class OperationConverter extends ClassicConverter {
        public static final String key = "requestExternalOperation";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
