package org.opensearch.graph.test;


import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;
import org.opensearch.client.transport.*;

public abstract class TestSetupBase {
    protected SearchEmbeddedNode instance;

    public void init() throws Exception {
        instance = GlobalSearchEmbeddedNode.getInstance();
        loadData(instance.getClient());
    }

    public void cleanup(){
        cleanData(instance.getClient());
    }


    protected abstract void loadData(TransportClient client) throws Exception;
    protected abstract void cleanData(TransportClient client);
}
