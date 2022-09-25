package org.opensearch.graph.services.engine2.data.schema.discrete;

import com.google.inject.Inject;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2TestSchemaProviderFactory implements GraphElementSchemaProviderFactory{
    public M2TestSchemaProviderFactory() {
        this.schemaProviders = new HashMap<>();
        this.schemaProviders.put("Dragons", new M2DragonsPhysicalSchemaProvider());
    }

    //region Constructors
    @Inject
    public M2TestSchemaProviderFactory(Config config, RawSchema schema) {
        this();
    }
    //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return this.schemaProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphElementSchemaProvider> schemaProviders;
    //endregion
}
