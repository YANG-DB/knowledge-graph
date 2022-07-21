package org.opensearch.graph.executor.cursor.discrete;


import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.logical.CompositeLogicalNode;
import org.opensearch.graph.model.logical.LogicalEdge;
import org.opensearch.graph.model.logical.LogicalNode;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.results.*;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;


public class GraphQLTraversalCursor extends PathsTraversalCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphQLTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphQLTraversalCursor(TraversalCursorContext context) {
        super(context);
    }
    //endregion


    protected Assignment<LogicalNode, LogicalEdge> toAssignment(Path path) {
        Assignment.Builder builder = Assignment.Builder.instance();
        Assignment<LogicalNode, LogicalEdge> newAssignment = new Assignment<>();

        List<Object> pathObjects = path.objects();
        List<Set<String>> pathlabels = path.labels();
        for (int objectIndex = 0; objectIndex < pathObjects.size(); objectIndex++) {
            Object pathObject = pathObjects.get(objectIndex);
            String pathLabel = pathlabels.get(objectIndex).iterator().next();

            if (Vertex.class.isAssignableFrom(pathObject.getClass())) {
                if(builder.getCurrentNode()!=null &&
                        builder.getCurrentEdge()!=null)   {
                    //get current edge label
                    String label = builder.getCurrentEdge().label();
                    //todo verify existing current node has a relational field names as the label
                    //put new node as child node of current node with named relation label
                    ((CompositeLogicalNode)builder.getCurrentNode())
                            .withChild(label, (LogicalNode) toEntity((Vertex) pathObject, this.eEntityBases.get(pathLabel)));
                } else {
                    builder.withEntity(toEntity((Vertex) pathObject, this.eEntityBases.get(pathLabel)));
                }
            } else if (Edge.class.isAssignableFrom(pathObject.getClass())) {
                Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(pathLabel);
                builder.withRelationship(toRelationship((Edge)pathObject,relTuple._1,relTuple._2,relTuple._3));
            } else {
                throw new UnsupportedOperationException("unexpected object in path");
            }
        }

        return builder.build();
    }

    @Override
    protected org.opensearch.graph.model.logical.Vertex toEntity(Vertex vertex, EEntityBase element) {
        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();
        return new CompositeLogicalNode(vertex.id().toString(),eType)
                .withTag(element.geteTag())
                .withProperties(properties);

    }
}
