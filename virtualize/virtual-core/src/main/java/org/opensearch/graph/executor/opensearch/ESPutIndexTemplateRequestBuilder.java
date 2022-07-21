
package org.opensearch.graph.executor.opensearch;



import org.opensearch.action.ActionFuture;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.OpenSearchClient;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ESPutIndexTemplateRequestBuilder extends PutIndexTemplateRequestBuilder {
    private String type;
    private Map<String, Object> mappings;

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action) {
        super(client, action);
    }

    public ESPutIndexTemplateRequestBuilder(OpenSearchClient client, PutIndexTemplateAction action, String name) {
        super(client, action, name);
    }

    @Override
    public PutIndexTemplateRequestBuilder addMapping(String type, Map<String, Object> source) {
        this.type = type;
        this.mappings = source;
        return this;
    }

    public Map<String, Object> getMappings() {
        return mappings;
    }

    public String getType() {
        return type;
    }

    @Override
    public PutIndexTemplateRequest request() {
        if (!Objects.isNull(mappings)) {
            super.request.mapping(type, mappings);
        }
        return super.request();
    }

    @Override
    public ActionFuture<AcknowledgedResponse> execute() {
        request();
        return super.execute();
    }

    public Map<String, Object> getMappingsProperties(String type) {
        try {
            Map<String, Object> map = (Map<String, Object>) mappings.get(type);
            return (Map<String, Object>) map.get("properties");
        }catch (Throwable notFound) {
            return Collections.emptyMap();
        }
    }
}
