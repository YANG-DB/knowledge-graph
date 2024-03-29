package org.opensearch.graph.model.resourceInfo;

/*-
 * #%L
 * opengraph-model
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






import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryType;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * resources
 * http://domain/opengraph/query/:id
 * http://domain/opengraph/query/:id/asg
 * http://domain/opengraph/query/:id/inner
 * http://domain/opengraph/query/:id/v1
 * http://domain/opengraph/query/:id/plan
 * http://domain/opengraph/query/:id/elastic
 * http://domain/opengraph/query/:id/cursor/:sequence
 * http://domain/opengraph/query/:id/cursor/:sequence/result/:sequence
 */
public class QueryResourceInfo extends ResourceInfoBase {

    //region Constructors
    public QueryResourceInfo() {
    }

    public QueryResourceInfo(QueryType type, String resourceUrl, String resourceId, String cursorStoreUrl, CursorResourceInfo... cursorResourceInfos) {
        this(type, resourceUrl, resourceId, cursorStoreUrl, Stream.of(cursorResourceInfos));
    }

    public QueryResourceInfo(QueryType type,String resourceUrl, String resourceId, String cursorStoreUrl, Iterable<CursorResourceInfo> cursorResourceInfos) {
        this(type, resourceUrl, resourceId, cursorStoreUrl, cursorResourceInfos, Collections.emptyList());
    }

    public QueryResourceInfo(QueryType type,String resourceUrl, String resourceId, String cursorStoreUrl, Iterable<CursorResourceInfo> cursorResourceInfos, Iterable<QueryResourceInfo> resourceInfos) {
        super(resourceUrl, resourceId);
        this.type = type;
        this.cursorStoreUrl = cursorStoreUrl;
        this.ontologyQueryUrl = resourceUrl + "/v1";
        this.asgUrl = resourceUrl + "/asg";
        this.explainPlanUrl = resourceUrl + "/plan";
        this.elasticQueryUrl = resourceUrl + "/elastic";
        this.cursorResourceInfos = cursorResourceInfos == null ? Collections.emptyList() : Stream.ofAll(cursorResourceInfos).toJavaList();
        this.innerUrlResourceInfos = resourceInfos == null ? Collections.emptyList() : Stream.ofAll(resourceInfos).toJavaList();
    }

    //endregion

    //region Properties
    public String getCursorStoreUrl() {
        return cursorStoreUrl;
    }

    public String getExplainPlanUrl() {
        return explainPlanUrl;
    }

    public String getOntologyQueryUrl() {
        return ontologyQueryUrl;
    }

    public String getAsgUrl() {
        return asgUrl;
    }

    public String getElasticQueryUrl() {
        return elasticQueryUrl;
    }

    public GraphError getError() {
        return error;
    }

    public QueryType getType() {
        return type;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<CursorResourceInfo> getCursorResourceInfos() {
        return cursorResourceInfos;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<QueryResourceInfo> getInnerUrlResourceInfos() {
        return innerUrlResourceInfos;
    }

    public QueryResourceInfo withInnerQueryResources(List<QueryResourceInfo> innerQuery) {
        this.innerUrlResourceInfos = innerQuery;
        return this;
    }

//endregion

    public QueryResourceInfo error(GraphError error) {
        QueryResourceInfo clone = new QueryResourceInfo(
                QueryType.concrete,
                this.getResourceUrl(),
                this.getResourceId(),
                this.getCursorStoreUrl(),
                this.cursorResourceInfos);

        clone.error = error;
        return clone;
    }

    //region Fields
    private String cursorStoreUrl;
    private String explainPlanUrl;
    private String ontologyQueryUrl;
    private String asgUrl;
    private String elasticQueryUrl;
    private QueryType type;

    private GraphError error;

    private List<QueryResourceInfo> innerUrlResourceInfos;
    private List<CursorResourceInfo> cursorResourceInfos;

    //endregion
}
