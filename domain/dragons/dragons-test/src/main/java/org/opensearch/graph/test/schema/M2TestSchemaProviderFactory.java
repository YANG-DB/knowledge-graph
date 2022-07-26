package org.opensearch.graph.test.schema;



import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class M2TestSchemaProviderFactory implements GraphElementSchemaProviderFactory{
    //region Constructors
    public M2TestSchemaProviderFactory() {
        this.schemaProviders = new HashMap<>();
        this.schemaProviders.put("Dragons", new M2DragonsPhysicalSchemaProvider());
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
