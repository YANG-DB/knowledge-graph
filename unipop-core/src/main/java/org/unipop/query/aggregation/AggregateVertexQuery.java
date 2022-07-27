package org.unipop.query.aggregation;

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








import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.process.group.traversal.SemanticKeyTraversal;
import org.unipop.process.group.traversal.SemanticReducerTraversal;
import org.unipop.process.group.traversal.SemanticValuesTraversal;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.StepDescriptor;
import org.unipop.query.controller.UniQueryController;
import org.unipop.query.VertexQuery;

import java.util.List;

public class AggregateVertexQuery extends AggregateQuery implements VertexQuery {

    private final List<Vertex> vertices;
    private final Direction direction;

    public AggregateVertexQuery(List<Vertex> vertices,
                                Direction direction,
                                PredicatesHolder predicates,
                                SemanticKeyTraversal key,
                                SemanticValuesTraversal values,
                                SemanticReducerTraversal reduce,
                                StepDescriptor stepDescriptor) {
        super(predicates, key, values, reduce, stepDescriptor);
        this.vertices = vertices;
        this.direction = direction;
    }

    @Override
    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public interface AggregateVertexController extends UniQueryController {
        void query(AggregateVertexQuery uniQuery);
    }

    @Override
    public String toString() {
        return "AggregateVertexQuery{" +
                "vertices=" + vertices +
                ", direction=" + direction +
                '}';
    }
}
