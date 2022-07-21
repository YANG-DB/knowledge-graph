package org.opensearch.graph.unipop.controller.promise.context;


import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.promise.*;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.promise.Promise;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.unipop.query.StepDescriptor;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by lior.perry on 27/03/2017.
 */
public class PromiseElementControllerContext extends ElementControllerContext.Impl {

    //region Constructors
    public PromiseElementControllerContext(
            UniGraph graph,
            StepDescriptor stepDescriptor,
            Iterable<Promise> promises,
            Optional<TraversalConstraint> constraint,
            Iterable<HasContainer> selectPHasContainers,
            GraphElementSchemaProvider schemaProvider,
            ElementType elementType,
            int limit) {
        super(graph,stepDescriptor, elementType, schemaProvider, constraint, selectPHasContainers, limit);
        this.promises = new ArrayList<>(Stream.ofAll(promises).toJavaList());
    }
    //endregion

    //region Properties
    public Iterable<Promise> getPromises() {
        return promises;
    }
    //endregion

    //region Fields
    private Iterable<Promise> promises;
    //endregion

}
