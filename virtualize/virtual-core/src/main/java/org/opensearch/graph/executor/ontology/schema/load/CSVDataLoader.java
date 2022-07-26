package org.opensearch.graph.executor.ontology.schema.load;





import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.LoadResponse;

import java.io.File;
import java.io.IOException;

public interface CSVDataLoader {

    LoadResponse<String, GraphError> load(String type, String label, File data, GraphDataLoader.Directive directive) throws IOException;

    /**
     * does:
     *      - given string representation of csv file,
     *      - convert into bulk set
     *      - commit to repository
     */
    LoadResponse<String, GraphError> load(String type, String label, String payload, GraphDataLoader.Directive directive) throws IOException;

}
