package org.opensearch.graph.test.framework.index;

/*-
 * #%L
 * test-framework
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
