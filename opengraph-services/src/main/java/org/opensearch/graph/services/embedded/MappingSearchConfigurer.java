package org.opensearch.graph.services.embedded;

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




import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.collection.Stream;
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Roman on 12/04/2017.
 */
public class MappingSearchConfigurer implements SearchIndexConfigurer {
    //region Constructors
    public MappingSearchConfigurer(String indexName, Map<String, Object> mappings) {
        this(Collections.singletonList(indexName), mappings);
    }

    public MappingSearchConfigurer(Iterable<String> indices, Map<String, Object> mappings) {
        this.indices = Stream.ofAll(indices).toJavaSet();
        this.mappings = mappings;
    }

    public MappingSearchConfigurer(String index, Mappings mappings) throws IOException {
        this(Collections.singletonList(index), mappings);
    }

    public MappingSearchConfigurer(Iterable<String> indices, Mappings mappings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.mappings = mapper.readValue(mapper.writeValueAsString(mappings), new TypeReference<Map<String, Object>>(){});
        this.indices = Stream.ofAll(indices).toJavaSet();
    }
    //endregion

    //region ElasticIndexConfigurer
    @Override
    public void configure(TransportClient client) {
        try {
            addMappings(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Private Methods
    protected void addMappings(TransportClient client) throws IOException {
        for(String indexName : this.indices) {
            CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate(indexName);
            Map<String, Object> allMappings = (Map<String, Object>) mappings.get("mappings");
            for (Map.Entry<String, Object> mapping : allMappings.entrySet()) {
                createIndexRequestBuilder.addMapping(mapping.getKey(), (Map<String, Object>) mapping.getValue());
            }

            createIndexRequestBuilder.setSettings(Settings.builder().put("index.store.type", "fs").build());

            try {
                createIndexRequestBuilder.execute().actionGet();
            } catch (ResourceAlreadyExistsException ex) {
                client.admin().indices().prepareDelete(indexName).execute().actionGet();
                createIndexRequestBuilder.execute().actionGet();
            }
        }
    }
    //endregion

    //region Fields
    private Iterable<String> indices;
    private Map<String, Object> mappings;
    //endregion
}
