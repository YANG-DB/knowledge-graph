package org.opensearch.graph.unipop.controller.utils.traversal;

/*-
 * #%L
 * virtual-unipop
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





import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.search.translation.M1QueryTranslator;
import org.opensearch.graph.unipop.controller.search.translation.PredicateQueryTranslator;
import org.opensearch.graph.unipop.step.BoostingStepWrapper;
import org.opensearch.graph.unipop.step.NestedStepWrapper;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.process.predicate.ExistsP;

import java.util.List;
import java.util.function.Supplier;

public class TraversalQueryTranslator extends TraversalVisitor<Boolean>{
    //region Constructor
    public TraversalQueryTranslator(
            QueryBuilder queryBuilder,
            AggregationBuilder aggregationBuilder, boolean shouldCache) {
        this.aggregationBuilder = aggregationBuilder;
        this.queryBuilder = queryBuilder;
        this.sequenceSupplier = () -> this.sequenceNumber++;
        this.shouldCache = shouldCache;
    }
    //endregion

    //Override Methods
    @Override
    protected Boolean visitRecursive(Object o) {
        this.queryBuilder.push();
        super.visitRecursive(o);
        this.queryBuilder.pop();
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitNotStep(NotStep<?> notStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentLabel = "mustNot_" + nextSequenceNumber;
        queryBuilder.bool().mustNot(currentLabel);

        super.visitNotStep(notStep);
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitOrStep(OrStep<?> orStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentBoolLabel = "bool_" + nextSequenceNumber;
        String currentFilterLabel = "filter_" + nextSequenceNumber;
        String currentShouldLabel = "should_" + nextSequenceNumber;
        queryBuilder.bool(currentBoolLabel);

        List<? extends Traversal.Admin<?, ?>> localChildren = orStep.getLocalChildren();
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        Stream<Tuple2<Traversal, Boolean>> isBoostingTraversal = Stream.ofAll(localChildren).map(t -> new Tuple2(t, boostingTraversalVisitor.visit(t)));
        Stream<Traversal> filters = isBoostingTraversal.filter(t -> !t._2).map(t -> t._1);
        Stream<Traversal> shouldFilters = isBoostingTraversal.filter(t -> t._2).map(t -> t._1);

        if(filters.size() > 0) {
            queryBuilder.filter(currentFilterLabel).bool().should();
            filters.forEach(f -> super.visitRecursive(f));
            queryBuilder.seek(currentBoolLabel);
        }
        queryBuilder.should(currentShouldLabel);
        shouldFilters.forEach(f -> super.visitRecursive(f));

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitAndStep(AndStep<?> andStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentBoolLabel = "bool_" + nextSequenceNumber;
        String currentFilterLabel = "filter_" + nextSequenceNumber;
        String currentMustLabel = "must_" + nextSequenceNumber;

        queryBuilder.bool(currentBoolLabel);
        List<? extends Traversal.Admin<?, ?>> localChildren = andStep.getLocalChildren();
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        Stream<Tuple2<Traversal, Boolean>> isBoostingTraversal = Stream.ofAll(localChildren).map(t -> new Tuple2(t, boostingTraversalVisitor.visit(t)));
        Stream<Traversal> filters = isBoostingTraversal.filter(t -> !t._2).map(t -> t._1);
        Stream<Traversal> mustFilters = isBoostingTraversal.filter(t -> t._2).map(t -> t._1);

        if(filters.size() > 0) {
            queryBuilder.filter(currentFilterLabel).bool().must();
            filters.forEach(f -> super.visitRecursive(f));
            queryBuilder.seek(currentBoolLabel);
        }
        queryBuilder.must(currentMustLabel);
        mustFilters.forEach(f -> super.visitRecursive(f));

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitHasStep(HasStep<?> hasStep) {
        PredicateQueryTranslator queryTranslator = M1QueryTranslator.instance;

        if (hasStep.getHasContainers().size() == 1) {
            queryBuilder = queryTranslator.translate(queryBuilder, aggregationBuilder,
                    hasStep.getHasContainers().get(0).getKey(), hasStep.getHasContainers().get(0).getPredicate());
        } else {
            int nextSequenceNumber = sequenceSupplier.get();
            String currentLabel = "must_" + nextSequenceNumber;
            queryBuilder.bool().must(currentLabel);

            hasStep.getHasContainers().forEach(hasContainer -> {
                queryBuilder.seek(currentLabel);
                queryBuilder = queryTranslator.translate(queryBuilder,aggregationBuilder , hasContainer.getKey(), hasContainer.getPredicate());
            });
        }

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitWhereStep(WherePredicateStep<?> wherePredicateStep) {
        return true;
    }

    @Override
    protected Boolean visitTraversalFilterStep(TraversalFilterStep<?> traversalFilterStep) {
        if (traversalFilterStep.getLocalChildren().size() == 1) {
            Traversal.Admin subTraversal = traversalFilterStep.getLocalChildren().get(0);
            if (subTraversal.getSteps().size() == 1
                    && PropertiesStep.class.isAssignableFrom(subTraversal.getSteps().get(0).getClass())) {
                PropertiesStep propertiesStep = (PropertiesStep) subTraversal.getSteps().get(0);

                if (propertiesStep.getPropertyKeys().length == 1) {
                    this.visitRecursive(new HasStep<>(null, new HasContainer(propertiesStep.getPropertyKeys()[0], new ExistsP<>())));
                } else {
                    int nextSequenceNumber = sequenceSupplier.get();
                    String currentLabel = "should_" + nextSequenceNumber;
                    queryBuilder.bool().should(currentLabel);

                    for (String key : propertiesStep.getPropertyKeys()) {
                        queryBuilder.seek(currentLabel);
                        this.visitRecursive(new HasStep<>(null, new HasContainer(key, new ExistsP<>())));
                    }
                }
            }
        }

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitBoostingStep(BoostingStepWrapper o) {
        queryBuilder.boost(o.getBoosting());
        super.visitBoostingStep(o);
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitNestedStep(NestedStepWrapper o) {
        queryBuilder.nested(o.geteType());
        super.visitNestedStep(o);
        return Boolean.TRUE;
    }

    //endregion

    //region Fields
    private QueryBuilder queryBuilder;
    private AggregationBuilder aggregationBuilder;
    private int sequenceNumber = 0;
    private Supplier<Integer> sequenceSupplier;

    private boolean shouldCache;
    //endregion
}
