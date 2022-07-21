package org.opensearch.graph.services.controllers;


import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.transport.ContentResponse;

import java.io.File;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface DataLoaderController {

    ContentResponse<String> init(String ontology);
    ContentResponse<String> createMapping(String ontology);
    ContentResponse<String> createIndices(String ontology);
    ContentResponse<String> drop(String ontology);


    /**
     *
     * @param ontology
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param type
     * @param label
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param type
     * @param label
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive);
}
