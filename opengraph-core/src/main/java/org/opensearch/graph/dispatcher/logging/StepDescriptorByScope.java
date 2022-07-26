package org.opensearch.graph.dispatcher.logging;







import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class StepDescriptorByScope {
    //region Static
    public static StepDescriptorByScope.MDCWriter of(String stepScope) {
        return new StepDescriptorByScope.MDCWriter(stepScope);
    }

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String requestScope) {
            super(StepDescriptorByScope.Converter.key, requestScope);
        }
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "stepScope";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
