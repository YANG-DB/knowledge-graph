package org.opensearch.graph.model.resourceInfo;




import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryType;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * resources
 * http://domain/fuse/query/:id
 * http://domain/fuse/query/:id/asg
 * http://domain/fuse/query/:id/inner
 * http://domain/fuse/query/:id/v1
 * http://domain/fuse/query/:id/plan
 * http://domain/fuse/query/:id/elastic
 * http://domain/fuse/query/:id/cursor/:sequence
 * http://domain/fuse/query/:id/cursor/:sequence/result/:sequence
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
        this.v1QueryUrl = resourceUrl + "/v1";
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

    public String getV1QueryUrl() {
        return v1QueryUrl;
    }

    public String getAsgUrl() {
        return asgUrl;
    }

    public String getElasticQueryUrl() {
        return elasticQueryUrl;
    }

    public FuseError getError() {
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

    public QueryResourceInfo error(FuseError error) {
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
    private String v1QueryUrl;
    private String asgUrl;
    private String elasticQueryUrl;
    private QueryType type;

    private FuseError error;

    private List<QueryResourceInfo> innerUrlResourceInfos;
    private List<CursorResourceInfo> cursorResourceInfos;

    //endregion
}
