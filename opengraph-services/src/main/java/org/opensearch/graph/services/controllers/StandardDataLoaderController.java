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




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.schema.load.CSVDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphInitiator;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardDataLoaderController implements DataLoaderController {
    //region Constructors
    @Inject
    public StandardDataLoaderController(OntologyProvider ontologyProvider,
                                        GraphInitiator initiator,
                                        CSVDataLoader csvDataLoader,
                                        GraphDataLoader graphDataLoader) {
        this.ontologyProvider = ontologyProvider;
        this.initiator = initiator;
        this.csvDataLoader = csvDataLoader;
        this.graphDataLoader = graphDataLoader;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, GraphError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.graphDataLoader.load(ontology, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, GraphError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, GraphError>() {
                        @Override
                        public List<CommitResponse<String, GraphError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, GraphError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<GraphError> getFailures() {
                                    return Collections.singletonList(new GraphError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, GraphError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, GraphError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.csvDataLoader.load(type,label , data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, GraphError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, GraphError>() {
                        @Override
                        public List<CommitResponse<String, GraphError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, GraphError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<GraphError> getFailures() {
                                    return Collections.singletonList(new GraphError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, GraphError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    public ContentResponse<LoadResponse<String, GraphError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, GraphError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.csvDataLoader.load(type, label, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, GraphError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, GraphError>() {
                        @Override
                        public List<CommitResponse<String, GraphError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, GraphError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<GraphError> getFailures() {
                                    return Collections.singletonList(new GraphError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, GraphError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    /**
     * does:
     *  - unzip file
     *  - split to multiple small files
     *  - for each file (in parallel)
     *      - convert into bulk set
     *      - commit to repository
     */
    public ContentResponse<LoadResponse<String, GraphError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, GraphError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.graphDataLoader.load(ontology, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, GraphError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, GraphError>() {
                        @Override
                        public List<CommitResponse<String, GraphError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, GraphError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<GraphError> getFailures() {
                                    return Collections.singletonList(new GraphError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, GraphError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    public ContentResponse<String> init(String ontology) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of("indices created:" + this.initiator.init(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<String> createMapping(String ontology) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of("mapping created:" + this.initiator.createTemplate(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<String> createIndices(String ontology) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of("indices created:" + this.initiator.createIndices(ontology)))
                .compose();
    }

    @Override
    public ContentResponse<String> drop(String ontology) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of("indices dropped:" + this.initiator.drop(ontology)))
                .compose();
    }
//endregion

    //region Private Methods
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private final GraphInitiator initiator;
    private final CSVDataLoader csvDataLoader;
    private GraphDataLoader graphDataLoader;

    //endregion

}
