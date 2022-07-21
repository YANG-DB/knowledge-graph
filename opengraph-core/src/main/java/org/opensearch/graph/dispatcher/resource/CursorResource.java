package org.opensearch.graph.dispatcher.resource;




import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.provision.CursorRuntimeProvision;
import org.opensearch.graph.dispatcher.profile.QueryProfileInfo;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lior.perry on 06/03/2017.
 */
public class CursorResource implements CursorRuntimeProvision {
    //region Constructors
    public CursorResource(String cursorId, Cursor cursor, QueryProfileInfo profileInfo, CreateCursorRequest cursorRequest) {
        this.cursorId = cursorId;
        this.profileInfo = profileInfo;
        this.pageResources = new HashMap<>();
        this.pageStatus = new HashMap<>();
        this.cursor = cursor;
        this.cursorRequest = cursorRequest;
        this.timeCreated = new Date(System.currentTimeMillis());
    }

        //endregion

    //region Public Methods
    public String getCursorId() {
        return this.cursorId;
    }

    public Date getTimeCreated() {
        return this.timeCreated;
    }

    public Iterable<PageResource> getPageResources() {
        return this.pageResources.values();
    }

    public Map<String, PageState> getPageStatus() {
        return pageStatus;
    }

    public Optional<PageResource> getPageResource(String pageId) {
        return Optional.ofNullable(this.pageResources.get(pageId));
    }

    public void addPageResource(String pageId, PageResource pageResource) {
        this.pageResources.put(pageId, pageResource);
        this.pageStatus.put(pageId, pageResource.getState());
    }

    public void deletePageResource(String pageId) {
        this.pageResources.remove(pageId);
        this.pageStatus.put(pageId,PageState.DELETED);
    }

    public String getNextPageId() {
        return String.valueOf(this.pageSequence.incrementAndGet());
    }

    public String getCurrentPageId() {
        return String.valueOf(this.pageSequence.get());
    }

    public String getPriorPageId() {
        return String.valueOf(this.pageSequence.get() > 0 ? this.pageSequence.get()-1 : 0);
    }

    public Cursor getCursor() {
        return cursor;
    }

    public QueryProfileInfo getProfileInfo() {
        return profileInfo;
    }

    public CreateCursorRequest getCursorRequest() {
        return this.cursorRequest;
    }


    @Override
    public int getActiveScrolls() {
        return getCursor().getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return getCursor().clearScrolls();
    }
    //endregion

    //region Fields
    private String cursorId;
    private QueryProfileInfo profileInfo;
    private CreateCursorRequest cursorRequest;
    private Cursor cursor;
    private Date timeCreated;

    private Map<String, PageResource> pageResources;
    private Map<String, PageState> pageStatus;
    //
    private AtomicInteger pageSequence = new AtomicInteger();

    //endregion
}
