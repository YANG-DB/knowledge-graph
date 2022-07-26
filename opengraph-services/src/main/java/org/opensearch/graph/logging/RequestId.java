package org.opensearch.graph.logging;




import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.opensearch.graph.dispatcher.logging.LogMessage;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class RequestId {
    public static LogMessage.MDCWriter of(String requestId) {
        return new LogMessage.MDCWriter.KeyValue(Converter.key, requestId);
    }

    public static class Converter extends ClassicConverter {
        public static final String key = "requestId";

        //region ClassicConverter Implementation
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return iLoggingEvent.getMDCPropertyMap().getOrDefault(key, "");
        }
        //endregion
    }
}
