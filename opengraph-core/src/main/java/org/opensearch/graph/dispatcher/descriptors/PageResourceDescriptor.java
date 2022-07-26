package org.opensearch.graph.dispatcher.descriptors;







import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.model.descriptors.Descriptor;

public class PageResourceDescriptor implements Descriptor<PageResource> {
    //region Descriptor Implementation
    @Override
    public String describe(PageResource item) {
        return String.format("Page{id: %s, requestedSize: %s}", item.getPageId(), item.getRequestedSize());
    }
    //endregion
}
