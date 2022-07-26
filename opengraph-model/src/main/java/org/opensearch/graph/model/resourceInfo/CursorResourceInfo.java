package org.opensearch.graph.model.resourceInfo;






import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.profile.QueryProfileStepInfoData;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class CursorResourceInfo extends ResourceInfoBase {
    //region Constructors
    public CursorResourceInfo() {}

    public CursorResourceInfo(
            String resourceUrl,
            String resourceId,
            CreateCursorRequest cursorRequest,
            String pageStoreUrl,
            PageResourceInfo...pageResourceInfos) {
        this(resourceUrl, resourceId, cursorRequest, pageStoreUrl, Stream.of(pageResourceInfos));
    }

    public CursorResourceInfo(
            String resourceUrl,
            String resourceId,
            CreateCursorRequest cursorRequest,
            String pageStoreUrl,
            Iterable<PageResourceInfo> pageResourceInfos) {
        super(resourceUrl,resourceId);
        this.pageStoreUrl = pageStoreUrl;
        this.cursorRequest = cursorRequest;
        this.pageResourceInfos = Stream.ofAll(pageResourceInfos).toJavaList();
    }

    public CursorResourceInfo(
            String resourceUrl,
            String resourceId,
            CreateCursorRequest cursorRequest,
            List<QueryProfileStepInfoData> profileInfo,
            String pageStoreUrl,
            Iterable<PageResourceInfo> pageResourceInfos) {
        super(resourceUrl,resourceId);
        this.profileInfo = profileInfo;
        this.pageStoreUrl = pageStoreUrl;
        this.cursorRequest = cursorRequest;
        this.pageResourceInfos = Stream.ofAll(pageResourceInfos).toJavaList();
    }
    //endregion

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<QueryProfileStepInfoData> getProfileInfo() {
        return profileInfo;
    }

    //region Properties

    public String getPageStoreUrl() {
        return this.pageStoreUrl;
    }

    public void setPageStoreUrl(String pageStoreUrl) {
        this.pageStoreUrl = pageStoreUrl;
    }

    public CreateCursorRequest getCursorRequest() {
        return cursorRequest;
    }

    public void setCursorRequest(CreateCursorRequest cursorRequest) {
        this.cursorRequest = cursorRequest;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<PageResourceInfo> getPageResourceInfos() {
        return pageResourceInfos;
    }

    public void setPageResourceInfos(List<PageResourceInfo> pageResourceInfos) {
        this.pageResourceInfos = pageResourceInfos;
    }

    public GraphError getError() {
        return error;
    }

    public CursorResourceInfo error(GraphError error) {
        CursorResourceInfo clone  = new CursorResourceInfo(
                this.getResourceUrl(),
                this.getResourceId(),
                this.cursorRequest,
                this.pageStoreUrl,
                this.pageResourceInfos);

        clone.error = error;
        return clone ;
    }


    //endregion

    //region Fields
    private GraphError error;
    private CreateCursorRequest cursorRequest;
    private String pageStoreUrl;
    private List<QueryProfileStepInfoData> profileInfo;
    private List<PageResourceInfo> pageResourceInfos;
    //endregion
}
