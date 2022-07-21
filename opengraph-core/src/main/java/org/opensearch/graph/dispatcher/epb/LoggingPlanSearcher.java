package org.opensearch.graph.dispatcher.epb;




import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.logging.*;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.slf4j.Logger;

public class LoggingPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q> {
    public static final String planSearcherParameter = "LoggingPlanSearcher.@descriptor";
    public static final String descriptorParameter = "LoggingPlanSearcher.@planSearcher";
    public static final String loggerParameter = "LoggingPlanSearcher.@logger";

    //region Constructors
    @Inject
    public LoggingPlanSearcher(
            @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
            Descriptor<PlanWithCost<P, C>> descriptor,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.planSearcher = planSearcher;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<P, C> search(Q query) {
        return new LoggingSyncMethodDecorator<PlanWithCost<P, C>>(this.logger, this.metricRegistry, search, LogMessage.Level.trace)
                .decorate(() -> {
                    PlanWithCost<P, C> planWithCost = this.planSearcher.search(query);
                    if (planWithCost != null) {
                        new LogMessage.Impl(this.logger, LogMessage.Level.debug, "execution plan: {}", sequence, LogType.of(LogType.log), search, ElapsedFrom.now())
                                .with(this.descriptor.describe(planWithCost)).log();
                    }
                    return planWithCost;
                });
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private PlanSearcher<P, C, Q> planSearcher;
    private Descriptor<PlanWithCost<P, C>> descriptor;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
