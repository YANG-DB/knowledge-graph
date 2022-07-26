package org.opensearch.graph.dispatcher.logging;





import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class StepDescriptorLogWriter {
    public static LogMessage.MDCWriter of(String description) {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, description);
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "stepDescriptor";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
