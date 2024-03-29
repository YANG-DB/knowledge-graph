//package org.unipop.process.count;

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







//
//import org.apache.tinkerpop.gremlin.process.traversal.Step;
//import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
//import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
//import org.apache.tinkerpop.gremlin.process.traversal.step.map.CountGlobalStep;
//import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.StartStep;
//import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
//import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
//import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
//import org.apache.tinkerpop.gremlin.structure.Graph;
//import org.unipop.process.predicate.PredicatesUtil;
//import org.unipop.process.start.UniGraphStartStep;
//import org.unipop.process.vertex.UniGraphVertexStep;
//import org.unipop.structure.UniGraph;
//
//import java.util.*;
//
//public class UniGraphCountStepStrategy extends AbstractTraversalStrategy<TraversalStrategy.ProviderOptimizationStrategy> implements TraversalStrategy.ProviderOptimizationStrategy {
//    //region AbstractTraversalStrategy Implementation
//    @Override
//    public Set<Class<? extends TraversalStrategy.ProviderOptimizationStrategy>> applyPrior() {
//        Set<Class<? extends TraversalStrategy.ProviderOptimizationStrategy>> priorStrategies = new HashSet<>();
//        priorStrategies.add(PredicatesUtil.class);
//        return priorStrategies;
//    }
//
//    @Override
//    public void apply(Traversal.Admin<?, ?> traversal) {
//        if(traversal.getEngine().isComputer()) return;
//
//        Graph graph = traversal.getGraph().get();
//        if(!(graph instanceof UniGraph)) return;
//
//        UniGraph elasticGraph = (UniGraph) graph;
//
//        TraversalHelper.getStepsOfAssignableClassRecursively(CountGlobalStep.class, traversal).forEach(step -> {
//            UniGraphCountStep elasticCountStep = null;
//            if (UniGraphVertexStep.class.isAssignableFrom(step.getPreviousStep().getClass())) {
//                UniGraphVertexStep elasticVertexStep = (UniGraphVertexStep)step.getPreviousStep();
//                elasticCountStep = new UniGraphCountStep(
//                        traversal,
//                        elasticVertexStep.getReturnClass(),
//                        elasticVertexStep.getPredicates(),
//                        new Object[0],
//                        elasticVertexStep.getEdgeLabels(),
//                        Optional.of(elasticVertexStep.getDirection()), elasticGraph.getControllerManager());
//
//            } else if (UniGraphStartStep.class.isAssignableFrom(step.getPreviousStep().getClass())) {
//                UniGraphStartStep elasticGraphStep = (UniGraphStartStep)step.getPreviousStep();
//                elasticCountStep = new UniGraphCountStep(
//                        traversal,
//                        elasticGraphStep.getReturnClass(),
//                        elasticGraphStep.getPredicates(),
//                        elasticGraphStep.getIds(),
//                        new String[0],
//                        Optional.empty(), elasticGraph.getControllerManager());
//            }
//
//            if (elasticCountStep != null) {
//                TraversalHelper.replaceStep(step.getPreviousStep(), elasticCountStep, traversal);
//                traversal.removeStep(step);
//
//                insertStartStepWhenTraversalIsInternal(traversal, elasticCountStep);
//            }
//        });
//    }
//    //endregion
//
//    //region Private Methods
//    private void insertStartStepWhenTraversalIsInternal(final Traversal.Admin<?, ?> traversal, Step step) {
//        if (!traversal.getParent().equals(EmptyStep.instance())) {
//            StartStep startStep = new StartStep(traversal);
//            TraversalHelper.insertBeforeStep(startStep, step, traversal);
//        }
//    }
//    //endregion
//}
