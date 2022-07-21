package org.opensearch.graph.services.controllers;


import org.opensearch.graph.model.resourceInfo.FuseResourceInfo;
import org.opensearch.graph.model.transport.ContentResponse;

/**
 * Created by Roman on 11/06/2017.
 */
public interface ApiDescriptionController
{
    /**
     * get general info
     * @return
     */
    ContentResponse<FuseResourceInfo> getInfo();
}
