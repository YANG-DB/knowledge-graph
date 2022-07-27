package org.opensearch.graph.dispatcher.gta;

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







import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.logging.LogMessage;
import org.opensearch.graph.dispatcher.logging.LoggingSyncMethodDecorator;
import org.opensearch.graph.dispatcher.logging.MethodName;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

public class LoggingPlanTraversalTranslator implements PlanTraversalTranslator {
    public static final String planTraversalTranslatorParameter = "LoggingPlanTraversalTranslator.@planTraversalTranslator";
    public static final String loggerParameter = "LoggingPlanTraversalTranslator.@logger";

    //region Constructors
    @Inject
    public LoggingPlanTraversalTranslator(
            @Named(planTraversalTranslatorParameter) PlanTraversalTranslator planTraversalTranslator,
            Descriptor<GraphTraversal<?, ?>> descriptor,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.innerTranslator = planTraversalTranslator;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanTraversalTranslator
    @Override
    public GraphTraversal<?, ?> translate(PlanWithCost<Plan, PlanDetailedCost> planWithCost, TranslationContext context) {
        return new LoggingSyncMethodDecorator<GraphTraversal<?, ?>>(this.logger, this.metricRegistry, translate, LogMessage.Level.trace)
                .decorate(() -> this.innerTranslator.translate(planWithCost, context));
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private PlanTraversalTranslator innerTranslator;
    private Descriptor<GraphTraversal<?, ?>> descriptor;

    private static MethodName.MDCWriter translate = MethodName.of("translate");
    //endregion
}
