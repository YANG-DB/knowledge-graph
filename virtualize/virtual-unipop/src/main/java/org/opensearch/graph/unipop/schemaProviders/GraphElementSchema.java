package org.opensearch.graph.unipop.schemaProviders;

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


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.model.schema.BaseTypeElement.NestedType;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.graph.unipop.step.NestingStepWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface GraphElementSchema {
    Class getSchemaElementType();

    Type getLabel();

    GraphElementConstraint getConstraint();

    Optional<GraphElementRouting> getRouting();

    Optional<IndexPartitions> getIndexPartitions();

    Iterable<GraphElementPropertySchema> getProperties();

    Optional<GraphElementPropertySchema> getProperty(Property property);

    /**
     * derive the has-type traversal constraint according to the actual given Type
     *
     * @param label
     * @return
     */
    static GraphElementConstraint.Impl generateConstraint(Type label) {
        if (NestedType.class.isAssignableFrom(label.getClass())) {
            GraphTraversal.Admin<Object, Object> admin = __.start().asAdmin();
            admin.addStep(new NestingStepWrapper(__.has(T.label, label.getName()).asAdmin().getStartStep(), ((NestedType) label).getPath()));
            return new GraphElementConstraint.Impl(admin);
        }
        return new GraphElementConstraint.Impl(__.has(T.label, label.getName()));
    }

    abstract class Impl implements GraphElementSchema {
        //region Constructors
        public Impl(Type label) {
            this(label,
                    generateConstraint(label),
                    Optional.empty(),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(Type label, GraphElementRouting routing) {
            this(label,
                    generateConstraint(label),
                    Optional.of(routing),
                    Optional.empty(),
                    Collections.emptyList());
        }

        public Impl(Type label, IndexPartitions indexPartitions) {
            this(label,
                    generateConstraint(label),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    Collections.emptyList());
        }

        public Impl(Type label,
                    GraphElementRouting routing,
                    IndexPartitions indexPartitions) {
            this(label, generateConstraint(label), Optional.of(routing), Optional.of(indexPartitions), Collections.emptyList());
        }

        public Impl(Type label,
                    IndexPartitions indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            this(label,
                    generateConstraint(label),
                    Optional.empty(),
                    Optional.of(indexPartitions),
                    properties);
        }

        public Impl(Type label,
                    GraphElementConstraint constraint,
                    Optional<GraphElementRouting> routing,
                    Optional<IndexPartitions> indexPartitions,
                    Iterable<GraphElementPropertySchema> properties) {
            this.label = label;
            this.constraint = constraint;
            this.routing = routing;
            this.indexPartitions = indexPartitions;
            this.properties = StreamSupport.stream(properties.spliterator(), false).collect(Collectors.toList());
        }
        //endregion

        //region GraphElementSchema Implementation
        @Override
        public Type getLabel() {
            return this.label;
        }

        @Override
        public Optional<GraphElementRouting> getRouting() {
            return this.routing;
        }

        @Override
        public Optional<IndexPartitions> getIndexPartitions() {
            return this.indexPartitions;
        }

        @Override
        public Iterable<GraphElementPropertySchema> getProperties() {
            return this.properties;
        }

        @Override
        public Optional<GraphElementPropertySchema> getProperty(Property property) {
            return properties.stream()
                    .filter(p -> p.getName().equals(property.getName()) &&
                            p.getType().equals(property.getType()) &&
                            p.getpType().equals(property.getpType()))
                    .findFirst();
        }

        @Override
        public GraphElementConstraint getConstraint() {
            return this.constraint;
        }
        //endregion

        //region Fields
        private Type label;
        private GraphElementConstraint constraint;
        private Optional<GraphElementRouting> routing;
        private Optional<IndexPartitions> indexPartitions;
        private List<GraphElementPropertySchema> properties;
        //endregion
    }
}
