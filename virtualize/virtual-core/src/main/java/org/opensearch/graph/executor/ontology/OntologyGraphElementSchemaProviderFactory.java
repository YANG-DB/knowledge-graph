package org.opensearch.graph.executor.ontology;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.OntologySchemaProvider;

public class OntologyGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "OntologyGraphElementSchemaProviderFactory.@schemaProviderFactory";

    //region Constructors
    @Inject
    public OntologyGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new OntologySchemaProvider(ontology, this.schemaProviderFactory.get(ontology));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
