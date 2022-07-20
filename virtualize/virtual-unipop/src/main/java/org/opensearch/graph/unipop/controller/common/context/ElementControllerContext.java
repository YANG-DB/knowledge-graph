package org.opensearch.graph.unipop.controller.common.context;

/*-
 * #%L
 * fuse-dv-unipop
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

import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.Optional;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public interface ElementControllerContext extends ConstraintContext, SchemaProviderContext, ElementContext,StepContext, GraphContext, LimitContext, SelectContext {
    class Impl implements ElementControllerContext {
        //region Constructors
        public Impl(
                UniGraph graph,
                StepDescriptor stepDescriptor,
                ElementType elementType,
                GraphElementSchemaProvider schemaProvider,
                Optional<TraversalConstraint> constraint,
                Iterable<HasContainer> selectPHasContainers,
                int limit) {
            this.graph = graph;
            this.stepDescriptor = stepDescriptor;
            this.elementType = elementType;
            this.schemaProvider = schemaProvider;
            this.constraint = constraint;
            this.selectPHasContainers = selectPHasContainers;
            this.limit = limit;
        }
        //endregion

        @Override
        public StepDescriptor getStepDescriptor() {
            return stepDescriptor;
        }

        //region ElementControllerContext Implementation
        @Override
        public UniGraph getGraph() {
            return graph;
        }

        @Override
        public ElementType getElementType() {
            return elementType;
        }

        @Override
        public GraphElementSchemaProvider getSchemaProvider() {
            return schemaProvider;
        }

        @Override
        public Optional<TraversalConstraint> getConstraint() {
            return constraint;
        }

        @Override
        public int getLimit() {
            return limit;
        }

        @Override
        public Iterable<HasContainer> getSelectPHasContainers() {
            return selectPHasContainers;
        }
        //endregion

        //region Fields
        private UniGraph graph;
        private StepDescriptor stepDescriptor;
        private ElementType elementType;
        private GraphElementSchemaProvider schemaProvider;
        private Optional<TraversalConstraint> constraint;
        private int limit;
        private Iterable<HasContainer> selectPHasContainers;
        //endregion
    }
}
