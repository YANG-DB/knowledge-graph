package org.unipop.process.start;

/*-
 * #%L
 * unipop-core
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








import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.unipop.process.Profiler;
import org.unipop.process.predicate.ReceivesPredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.unipop.process.Profiler.PROFILER;

public class UniGraphStartCountStep<S> extends AbstractStep<S, Long> implements ReceivesPredicatesHolder<S, Long> {
    //region Constructors
    public UniGraphStartCountStep(UniGraphStartStep uniGraphStartStep, ControllerManager controllerManager) {
        super(uniGraphStartStep.getTraversal());

        this.predicates = uniGraphStartStep.getPredicates();
        this.controllers = controllerManager.getControllers(ReduceQuery.SearchController.class);
        this.stepDescriptor = new StepDescriptor(this);
        this.traversal.getSteps().forEach(s->this.labels.addAll(((Step)s).getLabels()));
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<Long> processNextStart() throws NoSuchElementException {
        ReduceQuery reduceQuery = new ReduceQuery(this.predicates, this.stepDescriptor);
        Profiler profiler = this.getTraversal().getSideEffects().getOrCreate(PROFILER, Profiler.Noop::new);
        controllers.forEach(c->c.setProfiler(profiler));

        long count = Stream.ofAll(this.controllers)
                .map(controller -> controller.count(reduceQuery))
                .sum().longValue();

        return this.getTraversal().getTraverserGenerator().generate(null, this, 1L).split(count, this);
    }
    //endregion

    //region ReceivePredicateHolder Implementation
    @Override
    public void addPredicate(PredicatesHolder predicatesHolder) {
        this.predicates = PredicatesHolderFactory.and(this.predicates, predicatesHolder);
    }

    @Override
    public Set<String> getLabels() {
       return labels;
    }

    @Override
    public PredicatesHolder getPredicates() {
        return predicates;
    }

    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }
    //endregion

    //region Fields
    private List<ReduceQuery.SearchController> controllers;
    private PredicatesHolder predicates = PredicatesHolderFactory.empty();
    private int limit;
    private StepDescriptor stepDescriptor;
    private Set<String> labels = new HashSet<>();
    //endregion
}
