package org.opensearch.graph.dispatcher.logging;

/*-
 * #%L
 * opengraph-core
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







import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.MDC;

public interface LogMessage {
    Noop noop = new Noop();

    enum Level {
        trace,
        debug,
        info,
        warn,
        error
    }

    void log();
    LogMessage with(Object...args);

    class Noop implements LogMessage {
        //region LogMessage Implementation
        @Override
        public void log() {

        }

        @Override
        public LogMessage with(Object... args) {
            return null;
        }
        //endregion
    }

    interface MDCWriter {
        void write();

        class Noop implements MDCWriter {
            public static final Noop instance = new Noop();

            //region Constructors
            private Noop() {}
            //endregion

            //region MDCWriter Implementation
            @Override
            public void write() {

            }
            //endregion
        }

        class KeyValue implements MDCWriter {
            //region Constructors
            public KeyValue(String key, String value) {
                this.key = key;
                this.value =value;
            }
            //endregion

            //region MDCWriter Implementation
            @Override
            public void write() {
                MDC.put(this.key, this.value!=null ? this.value : "?");
            }
            //endregion

            //region Fields
            protected String key;
            protected String value;
            //endregion
        }

        class Composite implements MDCWriter {
            //region Static
            public static Composite of(MDCWriter...writers) {
                return new Composite(writers);
            }
            //endregion

            //region Constructors
            public Composite(MDCWriter...writers) {
                this(Stream.of(writers));
            }

            public Composite(Iterable<MDCWriter> writers) {
                this.writers = writers;
            }
            //endregion

            //region MDCWriter Implementation
            @Override
            public void write() {
                for(MDCWriter writer : this.writers) {
                    writer.write();
                }
            }
            //endregion

            //region Fields
            private Iterable<MDCWriter> writers;
            //endregion
        }
    }

    class Impl implements LogMessage {
        //region Constructors
        public Impl(Logger logger, Level level, String message, MDCWriter...mdcWriters) {
            this.logger = logger;
            this.level = level;
            this.message = message;
            this.args = Stream.empty();
            this.mdcWriters = Stream.of(mdcWriters);
        }
        //endregion

        //region Public Methods
        @Override
        public void log() {
            switch (this.level) {
                case trace: if (!this.logger.isTraceEnabled()) return;
                    break;
                case debug: if (!this.logger.isDebugEnabled()) return;
                    break;
                case info: if (!this.logger.isInfoEnabled()) return;
                    break;
                case warn: if (!this.logger.isWarnEnabled()) return;
                    break;
                case error: if (!this.logger.isErrorEnabled()) return;
                    break;
            }

            if (this.logger.getClass().equals(ch.qos.logback.classic.Logger.class)) {
                if (!((ch.qos.logback.classic.Logger)this.logger).isAdditive()) {
                    return;
                }
            }

            this.mdcWriters.forEach(MDCWriter::write);

            switch (this.level) {
                case trace:
                    this.logger.trace(this.message, this.args.toJavaArray());
                    break;
                case debug:
                    this.logger.debug(this.message, this.args.toJavaArray());
                    break;
                case info:
                    this.logger.info(this.message, this.args.toJavaArray());
                    break;
                case warn:
                    this.logger.warn(this.message, this.args.toJavaArray());
                    break;
                case error:
                    this.logger.error(this.message, this.args.toJavaArray());
                    break;
            }
        }

        @Override
        public LogMessage with(Object...args) {
            this.args = this.args.appendAll(Stream.of(args));
            return this;
        }
        //endregion

        //region Fields
        private String message;
        private Stream<Object> args;
        private Logger logger;
        private Level level;
        private Iterable<MDCWriter> mdcWriters;
        //endregion
    }
}
