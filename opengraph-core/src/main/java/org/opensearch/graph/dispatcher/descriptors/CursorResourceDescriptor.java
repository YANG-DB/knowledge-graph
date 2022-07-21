package org.opensearch.graph.dispatcher.descriptors;





import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.model.descriptors.Descriptor;

public class CursorResourceDescriptor implements Descriptor<CursorResource> {
    //region Descriptor Implementation
    @Override
    public String describe(CursorResource item) {
        return String.format("Cursor{id: %s, type: %s}", item.getCursorId(), item.getCursorRequest());
    }
    //endregion
}
