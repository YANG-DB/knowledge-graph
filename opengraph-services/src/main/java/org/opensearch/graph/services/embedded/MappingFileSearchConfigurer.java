package org.opensearch.graph.services.embedded;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by moti on 09/04/2017.
 */
public class MappingFileSearchConfigurer extends MappingSearchConfigurer {
    //region Constructors
    public MappingFileSearchConfigurer(String indexName, String mappingsFile) throws IOException {
        super(indexName, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }

    public MappingFileSearchConfigurer(Iterable<String> indices, String mappingsFile) throws IOException {
        super(indices, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }
    //endregion
}
