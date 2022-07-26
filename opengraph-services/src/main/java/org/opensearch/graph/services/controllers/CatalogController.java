package org.opensearch.graph.services.controllers;




import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.transport.ContentResponse;

import java.util.List;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface CatalogController {

    /**
     * get ontology resource by id
     * @param id
     * @return
     */
    ContentResponse<Ontology> getOntology(String id);

    /**
     * create new ontology
     * @param ontology
     * @return
     */
    ContentResponse<Ontology> addOntology(Ontology ontology);

    /**
     * get all ontologies
     * @return
     */
    ContentResponse<List<Ontology>> getOntologies();

    /**
     * get the physical schema by id
     * @param id
     * @return
     */
    ContentResponse<String> getSchema(String id);

    /**
     * get all the physical schemas
     * @return
     */
    ContentResponse<List<String>> getSchemas();
}
