package org.opensearch.graph.test.framework.index;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MappingFileConfigurer extends MappingEngineConfigurer {
    //region Constructors
    public MappingFileConfigurer(String indexName, String mappingsFile) throws IOException {
        super(indexName, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }

    public MappingFileConfigurer(Iterable<String> indices, String mappingsFile) throws IOException {
        super(indices, (Map<String, Object>) new ObjectMapper().readValue(new File(mappingsFile), new TypeReference<Map<String, Object>>(){}));
    }
    //endregion
}
