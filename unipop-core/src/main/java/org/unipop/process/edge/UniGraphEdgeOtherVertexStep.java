package org.unipop.process.edge;

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




import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.Profiling;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.javatuples.Pair;
import org.unipop.process.UniBulkStep;
import org.unipop.process.UniPredicatesStep;
import org.unipop.process.order.Orderable;
import org.unipop.process.properties.PropertyFetcher;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.search.DeferredVertexQuery;
import org.unipop.schema.reference.DeferredVertex;
import org.unipop.structure.UniEdge;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.stream.Collectors;

public class UniGraphEdgeOtherVertexStep extends UniPredicatesStep<Edge, Vertex> implements Orderable, Profiling {
    private List<DeferredVertexQuery.DeferredVertexController> deferredVertexControllers;
    private StepDescriptor stepDescriptor;
    private List<Pair<String, Order>> orders;

    public UniGraphEdgeOtherVertexStep(Traversal.Admin traversal, UniGraph graph, ControllerManager controllerManager) {
        super(traversal, graph);
        this.deferredVertexControllers = controllerManager.getControllers(DeferredVertexQuery.DeferredVertexController.class);
        this.stepDescriptor = new StepDescriptor(this);
    }

    @Override
    protected Iterator<Traverser.Admin<Vertex>> process(List<Traverser.Admin<Edge>> traversers) {
        List<Traverser.Admin<Vertex>> vertices = new ArrayList<>();

        traversers.forEach(traverser -> {
            UniEdge uniEdge = (UniEdge)traverser.get();
            if (uniEdge.otherVertex() != null) {
                vertices.add(traverser.split(uniEdge.otherVertex(), this));
            } else {

                final List<Object> objects = traverser.path().objects();
                if (objects.get(objects.size() - 2) instanceof Vertex) {
                    Vertex vertex = ElementHelper.areEqual((Vertex) objects.get(objects.size() - 2), traverser.get().outVertex()) ?
                            traverser.get().inVertex() :
                            traverser.get().outVertex();
                    vertices.add(traverser.split(vertex, this));
                }
            }
        });

        if (propertyKeys == null || propertyKeys.size() > 0){
            List<DeferredVertex> v = vertices.stream().map(Attachable::get)
                    .filter(vertex -> vertex instanceof DeferredVertex)
                    .map(vertex -> ((DeferredVertex) vertex))
                    .filter(DeferredVertex::isDeferred)
                    .collect(Collectors.toList());
            if (v.size() > 0) {
                DeferredVertexQuery query = new DeferredVertexQuery(v, propertyKeys, orders, stepDescriptor);
                deferredVertexControllers.forEach(deferredVertexController -> deferredVertexController.fetchProperties(query));
            }
        }

        return vertices.iterator();
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return Collections.singleton(TraverserRequirement.PATH);
    }

    @Override
    public void setMetrics(MutableMetrics metrics) {
        this.stepDescriptor = new StepDescriptor(this, metrics);
    }

    @Override
    public void setOrders(List<Pair<String, Order>> orders) {
        this.orders = orders;
    }
}
