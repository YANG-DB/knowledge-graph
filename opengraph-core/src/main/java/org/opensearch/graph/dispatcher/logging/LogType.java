package org.opensearch.graph.dispatcher.logging;




import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public enum LogType{
    start,
    log,
    metric,
    success,
    failure;

    public static LogMessage.MDCWriter of(LogType logType) {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, logType.toString());
    }

    public static class Converter  extends ClassicConverter {
        public static final String key = "logType";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
