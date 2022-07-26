package org.opensearch.graph.dispatcher.logging;







import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MethodName{
    //region Static
    public static MDCWriter of(String methodName) {
        return new MDCWriter(methodName);
    }
    //endregion

    public static class MDCWriter extends LogMessage.MDCWriter.KeyValue {
        //region Constructors
        public MDCWriter(String methodName) {
            super(Converter.key, methodName);
        }
        //endregion

        //region Properties
        public String getMethodName() {
            return this.value;
        }
        //endregion

        //region Override Methods
        @Override
        public String toString() {
            return this.value;
        }
        //endregion
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "methodName";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().get(key);
        }
        //endregion
    }
}
