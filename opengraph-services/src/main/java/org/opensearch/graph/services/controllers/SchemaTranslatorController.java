package org.opensearch.graph.services.controllers;


import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;

public interface SchemaTranslatorController {
    /**
     * translate the graphQL schema to Ontology schema
     *
     * @param ontology
     * @param graphqlschema
     * @return
     */
    ContentResponse<Ontology> translate(String ontology, String graphqlschema);

    /**
     * transform the ontology schema to a graphQL schema
     * @return
     */
    ContentResponse<String> transform(String ontologyId);

}
