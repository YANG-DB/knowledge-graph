package org.unipop.process.union;

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








import com.google.common.collect.Sets;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.RepeatStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.UnionStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.GroupStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SelectOneStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SelectStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.ReducingBarrierStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.unipop.process.edge.EdgeStepsStrategy;
import org.unipop.process.predicate.PredicatesUtil;
import org.unipop.process.properties.UniGraphPropertiesStrategy;
import org.unipop.process.repeat.UniGraphRepeatStepStrategy;
import org.unipop.process.start.UniGraphStartStepStrategy;
import org.unipop.process.vertex.UniGraphVertexStep;
import org.unipop.process.vertex.UniGraphVertexStepStrategy;
import org.unipop.structure.UniGraph;

import java.util.Set;

public class UniGraphUnionStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
    @Override
    public Set<Class<? extends ProviderOptimizationStrategy>> applyPrior() {
        return Sets.newHashSet(UniGraphStartStepStrategy.class, UniGraphVertexStepStrategy.class, UniGraphRepeatStepStrategy.class, EdgeStepsStrategy.class, UniGraphPropertiesStrategy.class);
    }

    @Override
    public void apply(Traversal.Admin<?, ?> traversal) {
        Graph graph = traversal.getGraph().get();
        if (!(graph instanceof UniGraph)) {
            return;
        }

        UniGraph uniGraph = (UniGraph) graph;

        TraversalHelper.getStepsOfClass(UnionStep.class, traversal).forEach(unionStep -> {
            Traversal.Admin[] traversals = (Traversal.Admin[]) unionStep.getGlobalChildren().toArray(new Traversal.Admin[0]);
            for (Traversal.Admin admin : traversals) {
                if (TraversalHelper.getLastStepOfAssignableClass(ReducingBarrierStep.class, admin).isPresent() ||
                        traversal.getParent() instanceof RepeatStep)
                    return;
            }
            UniGraphUnionStep uniGraphUnionStep = new UniGraphUnionStep(traversal, uniGraph, traversals);
            if (TraversalHelper.stepIndex(unionStep, traversal) != -1) {
                TraversalHelper.replaceStep(unionStep, uniGraphUnionStep, traversal);
            } else {
                TraversalHelper.getStepsOfAssignableClass(TraversalParent.class, traversal).forEach(traversalParent -> {
                    traversalParent.getLocalChildren().forEach(child -> {
                        if(TraversalHelper.stepIndex(unionStep, child) != -1) {
                            TraversalHelper.replaceStep(unionStep, uniGraphUnionStep, child);
                        }
                    });
                    traversalParent.getGlobalChildren().forEach(child -> {
                        if(TraversalHelper.stepIndex(unionStep, child) != -1) {
                            TraversalHelper.replaceStep(unionStep, uniGraphUnionStep, child);
                        }
                    });
                });
            }
        });
    }
}
