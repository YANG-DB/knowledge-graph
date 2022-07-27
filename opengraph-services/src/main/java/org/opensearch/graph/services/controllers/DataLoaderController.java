package org.opensearch.graph.services.controllers;

/*-
 * #%L
 * opengraph-services
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.GraphError;
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
    ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param type
     * @param label
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive);

    /**
     *
     * @param ontology
     * @param type
     * @param label
     * @param data
     * @param directive
     * @return
     */
    ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive);
}
