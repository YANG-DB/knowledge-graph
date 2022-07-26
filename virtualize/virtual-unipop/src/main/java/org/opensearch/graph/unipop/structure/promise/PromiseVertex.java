package org.opensearch.graph.unipop.structure.promise;





import org.opensearch.graph.unipop.controller.utils.map.MapBuilder;
import org.opensearch.graph.unipop.promise.Constraint;
import org.opensearch.graph.unipop.promise.Promise;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.structure.UniGraph;
import org.unipop.structure.UniVertex;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PromiseVertex extends UniVertex {
    //region Constructor
    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph) {
        this(promise, constraint, graph, Collections.emptyMap());
    }

    public PromiseVertex(Promise promise, Optional<Constraint> constraint, UniGraph graph, Map<String, Object> properties) {
        super(new MapBuilder<>(properties).put(T.id.getAccessor(), promise.getId()).get(), graph);

        this.promise = promise;
        this.constraint = constraint;
    }
    //endregion

    //region Override Methods
    @Override
    protected String getDefaultLabel() {
        return "promise";
    }
    //endregion

    //region properties
    public Promise getPromise() {
        return this.promise;
    }

    public Optional<Constraint> getConstraint() {

        return this.constraint;
    }
    //endregion

    //region Fields
    private Promise promise;
    private Optional<Constraint> constraint;
    //endregion
}
