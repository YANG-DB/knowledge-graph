package org.opensearch.graph.services.controllers;




import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.graph.model.transport.ContentResponse;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface DashboardController<C,D> extends Controller<C,D>{

    ContentResponse<ObjectNode> graphElementCount(String ontology);

    ContentResponse<ObjectNode> graphElementCreatedOverTime(String ontology);

    ContentResponse<ObjectNode> cursorCount();
}
