package org.opensearch.graph.services.engine2.data.schema.promise;

import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class DragonsPhysicalSchemaProvider extends GraphElementSchemaProvider.Impl {
    public DragonsPhysicalSchemaProvider() {
        super(
                Arrays.asList(
                        new GraphVertexSchema.Impl(Type.of("Person"), new StaticIndexPartitions("person")),
                        new GraphVertexSchema.Impl(Type.of("Dragon"), new StaticIndexPartitions("dragon")),
                        new GraphVertexSchema.Impl(Type.of("Kingdom"), new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl(Type.of("Horse"), new StaticIndexPartitions()),
                        new GraphVertexSchema.Impl(Type.of("Guild"), new StaticIndexPartitions())
                ),
                Collections.singletonList(new GraphEdgeSchema.Impl(
                        Type.of("fire"),
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
                        Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                        new StaticIndexPartitions(Arrays.asList(
                                FIRE.getName().toLowerCase() + "20170511",
                                FIRE.getName().toLowerCase() + "20170512",
                                FIRE.getName().toLowerCase() + "20170513"))))
        );
    }
    //endregion
}
