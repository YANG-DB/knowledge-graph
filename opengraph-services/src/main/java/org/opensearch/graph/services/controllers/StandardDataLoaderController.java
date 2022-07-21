package org.opensearch.graph.services.controllers;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.schema.load.CSVDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphInitiator;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.FuseError;
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
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, LogicalGraphModel data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.graphDataLoader.load(ontology, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, FuseError>() {
                        @Override
                        public List<CommitResponse<String, FuseError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<FuseError> getFailures() {
                                    return Collections.singletonList(new FuseError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, FuseError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, String data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.csvDataLoader.load(type,label , data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, FuseError>() {
                        @Override
                        public List<CommitResponse<String, FuseError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<FuseError> getFailures() {
                                    return Collections.singletonList(new FuseError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, FuseError> response) {
                            return this;
                        }
                    }))
                    .compose();
        }
    }

    @Override
    public ContentResponse<LoadResponse<String, FuseError>> loadCsv(String ontology, String type, String label, File data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.csvDataLoader.load(type, label, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, FuseError>() {
                        @Override
                        public List<CommitResponse<String, FuseError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<FuseError> getFailures() {
                                    return Collections.singletonList(new FuseError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, FuseError> response) {
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
    public ContentResponse<LoadResponse<String, FuseError>> loadGraph(String ontology, File data, GraphDataLoader.Directive directive) {
        try {
            return Builder.<LoadResponse<String, FuseError>>builder(OK, NOT_FOUND)
                    .data(Optional.of(this.graphDataLoader.load(ontology, data, directive)))
                    .compose();
        } catch (IOException e) {
            return Builder.<LoadResponse<String, FuseError>>builder(BAD_REQUEST, NOT_FOUND)
                    .data(Optional.of(new LoadResponse<String, FuseError>() {
                        @Override
                        public List<CommitResponse<String, FuseError>> getResponses() {
                            return Collections.singletonList(new CommitResponse<String, FuseError>() {
                                @Override
                                public List<String> getSuccesses() {
                                    return Collections.emptyList();
                                }

                                @Override
                                public List<FuseError> getFailures() {
                                    return Collections.singletonList(new FuseError(e.getMessage(), e));
                                }
                            });
                        }

                        @Override
                        public LoadResponse response(CommitResponse<String, FuseError> response) {
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
