package org.opensearch.graph.services.engine2.data.schema.discrete;

import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;
import static org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema.Application.endA;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2DragonsPhysicalSchemaProvider extends GraphElementSchemaProvider.Impl {
    public M2DragonsPhysicalSchemaProvider() {
        super(
                Arrays.asList(
                        new GraphVertexSchema.Impl(Type.of("Person"), new StaticIndexPartitions("person")),
                        new GraphVertexSchema.Impl(Type.of("Dragon"), new StaticIndexPartitions("dragon")),
                        new GraphVertexSchema.Impl(Type.of("Kingdom"), new StaticIndexPartitions("kingdom")),
                        new GraphVertexSchema.Impl(Type.of("Horse"), new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl(Type.of("Guild"), new StaticIndexPartitions())
                ),
                Arrays.asList(
                        new GraphEdgeSchema.Impl(
                                Type.of("fire"),
                                new GraphElementConstraint.Impl(__.has(T.label, "fire")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase() + "20170511",
                                        FIRE.getName().toLowerCase() + "20170512",
                                        FIRE.getName().toLowerCase() + "20170513"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                Type.of("fire"),
                                new GraphElementConstraint.Impl(__.has(T.label, "fire")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList(
                                        FIRE.getName().toLowerCase() + "20170511",
                                        FIRE.getName().toLowerCase() + "20170512",
                                        FIRE.getName().toLowerCase() + "20170513"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                Type.of("originatedIn"),
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of("Kingdom"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet()),
                        new GraphEdgeSchema.Impl(
                                Type.of("originatedIn"),
                                new GraphElementConstraint.Impl(__.has(T.label, "originatedIn")),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of("Kingdom"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of("Dragon"),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
                                        ))),
                                Direction.IN,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "OUT", "IN")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Arrays.asList("originated_in"))),
                                Collections.emptyList(),
                                Stream.of(endA).toJavaSet())
                        )
        );
    }
}
