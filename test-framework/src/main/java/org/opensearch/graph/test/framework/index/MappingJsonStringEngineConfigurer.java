package org.opensearch.graph.test.framework.index;





import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class MappingJsonStringEngineConfigurer extends MappingEngineConfigurer {
    //region Constructors
    public MappingJsonStringEngineConfigurer(String indexName, String mappingJson) throws IOException {
        super(indexName,  (Map<String, Object>) new ObjectMapper().readValue(mappingJson, new TypeReference<Map<String, Object>>(){}));
    }

    public MappingJsonStringEngineConfigurer(Iterable<String> indices, String mappingJson) throws IOException {
        super(indices,  (Map<String, Object>)new ObjectMapper().readValue(mappingJson, new TypeReference<Map<String, Object>>(){}));
    }
    //endregion
}
