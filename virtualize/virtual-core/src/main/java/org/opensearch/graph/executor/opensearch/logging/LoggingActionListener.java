package org.opensearch.graph.executor.opensearch.logging;





import com.codahale.metrics.Meter;
import org.opensearch.graph.dispatcher.logging.LogMessage;
import org.opensearch.action.ActionListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public class LoggingActionListener<TResponse> implements ActionListener<TResponse> {
    //region Constructors
    public LoggingActionListener(
            Function<TResponse, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Closeable timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.innerActionListener = Optional.empty();
        this.innerFailureMessage = Optional.empty();
        this.timerContext = timerContext;
        this.successMeter = successMeter;
        this.failureMeter = failureMeter;
    }

    public LoggingActionListener(
            ActionListener<TResponse> innerActionListener,
           Function<TResponse, LogMessage> successMessage,
            Function<Exception, LogMessage> failureMessage,
            Function<Exception, LogMessage> innerFailureMessage,
            Closeable timerContext,
            Meter successMeter,
            Meter failureMeter) {
        this(successMessage, failureMessage, timerContext, successMeter, failureMeter);
        this.innerActionListener = Optional.ofNullable(innerActionListener);
        this.innerFailureMessage = Optional.ofNullable(innerFailureMessage);
    }
    //endregion

    //region ActionListsner Implementation
    @Override
    public void onResponse(TResponse tResponse) {
        try {
            this.timerContext.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onResponse(tResponse));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.apply(ex).log());
        } finally {
            this.successMessage.apply(tResponse).log();
            this.successMeter.mark();
        }
    }

    @Override
    public void onFailure(Exception e) {
        try {
            this.timerContext.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            innerActionListener.ifPresent(tResponseActionListener -> tResponseActionListener.onFailure(e));
        } catch (Exception ex) {
            innerFailureMessage.ifPresent(logMessage -> logMessage.apply(ex).log());
        } finally {
            this.failureMessage.apply(e).log();
            this.failureMeter.mark();
        }
    }
    //endregion

    //region Fields
    private Function<TResponse, LogMessage> successMessage;
    private Function<Exception, LogMessage> failureMessage;
    private Closeable timerContext;
    private Meter successMeter;
    private Meter failureMeter;
    private Optional<ActionListener<TResponse>> innerActionListener;
    private Optional<Function<Exception, LogMessage>> innerFailureMessage;
    //endregion
}
