package org.opensearch.graph.epb.plan;

/*-
 * #%L
 * virtual-epb
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





import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.dispatcher.epb.PlanSearcher;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.epb.plan.query.AsgUnionSplitQueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.UnionOp;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.opensearch.graph.asg.AsgQueryTransformer;
import org.opensearch.graph.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import org.opensearch.graph.asg.strategy.propertyGrouping.Quant1AllQuantGroupingAsgStrategy;
import org.opensearch.graph.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import org.opensearch.graph.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import org.opensearch.graph.asg.strategy.selection.DefaultRelationSelectionAsgStrategy;
import org.opensearch.graph.asg.strategy.selection.DefaultSelectionAsgStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnionPlanSearcher implements PlanSearcher<Plan, PlanDetailedCost, AsgQuery> {
    public static final String planSearcherParameter = "UnionPlanSearcher.@planSearcherParameter";
    private AsgUnionSplitQueryTransformer splitQueryTransformer;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher;
    private QueryValidator<AsgQuery> queryValidator;
    @Inject
    public UnionPlanSearcher(@Named(planSearcherParameter) PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher,QueryValidator<AsgQuery> queryValidator, OntologyProvider ontologyProvider ) {
        this.mainPlanSearcher = mainPlanSearcher;
        this.queryValidator = queryValidator;
        final AsgQueryTransformer transformer = new AsgQueryTransformer(() -> Arrays.asList(
                new Quant1AllQuantGroupingAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new DefaultSelectionAsgStrategy(ontologyProvider),
                new DefaultRelationSelectionAsgStrategy(ontologyProvider)),
                ontologyProvider );
        this.splitQueryTransformer = new AsgUnionSplitQueryTransformer(transformer);
    }

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> search(AsgQuery query) {
        // generate multiple union plans (each plan is free from some-quant)
        final Iterable<AsgQuery> queries = splitQueryTransformer.transform(query);
        List<ValidationResult> results = Stream.ofAll(queries).map(q -> queryValidator.validate(q)).toJavaList();

        if(!Stream.ofAll(results).filter(r->!r.valid()).toJavaList().isEmpty()) {
            String errors = results.stream().filter(r -> !r.valid()).map(ValidationResult::toString).collect(Collectors.joining(","));
            throw new IllegalStateException("UnionPlanSearcher splitQueryTransformer - One of the plans not valid " + errors);
        }

        //plan main query
        final List<PlanWithCost<Plan, PlanDetailedCost>> plans = Stream.ofAll(queries)
                .map(q -> mainPlanSearcher.search(q))
                .filter(Objects::nonNull)
                .toJavaList();


        if (!Stream.ofAll(plans).filter(Objects::isNull).isEmpty()) {
            throw new IllegalStateException("UnionPlanSearcher - One of the plans is empty");
        }

        if (plans.size() == 0) {
            throw new IllegalStateException("UnionPlanSearcher - No valid plan was found");
        }

        if (plans.size() == 1) {
            return plans.get(0);
        }

        //use UnionOp to collect all resulted plans

        final Stream<PlanWithCost<Plan, PlanDetailedCost>> stream = Stream.ofAll(plans)
                .filter(Objects::nonNull)
                .filter(p->p.getCost()!=null && p.getCost().getGlobalCost()!=null);

        final double sumCosts = stream.map(p -> p.getCost().getGlobalCost().getCost()).sum().doubleValue();
        final List<PlanWithCost<Plan, CountEstimatesCost>> costs = stream
                .map(p -> new PlanWithCost<>(p.getPlan(),
                        new CountEstimatesCost(p.getCost().getGlobalCost().getCost(), 0)))
                .sortBy(p->p.getPlan().getOps().size())
                .toJavaList();

        final PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(sumCosts), costs);
        final PlanWithCost<Plan, PlanDetailedCost> unionPlan = new PlanWithCost<>(new Plan(), planDetailedCost);
        //add unionOp to union plan
        unionPlan.getPlan().getOps().add(new UnionOp(stream.map(p -> p.getPlan().getOps()).toJavaList()));

        return unionPlan;
    }


}
