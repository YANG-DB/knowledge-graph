package org.opensearch.graph.services.engine2.data.schema;

import com.google.inject.Inject;
import org.opensearch.graph.executor.ontology.schema.load.CSVDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import com.typesafe.config.Config;
import org.opensearch.graph.model.resourceInfo.GraphError;

import java.io.File;
import java.io.IOException;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class InitialTestDataLoader implements GraphDataLoader, CSVDataLoader {

    public InitialTestDataLoader() {
    }

    @Inject
    public InitialTestDataLoader(Config config, RawSchema schema) {
    }

    @Override
    public LoadResponse load(String ontology, LogicalGraphModel root, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse load(String ontology, File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse<String, GraphError> load(String type, String label, File data, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }

    @Override
    public LoadResponse<String, GraphError> load(String type, String label, String payload, Directive directive) throws IOException {
        return LoadResponse.EMPTY;
    }
}
