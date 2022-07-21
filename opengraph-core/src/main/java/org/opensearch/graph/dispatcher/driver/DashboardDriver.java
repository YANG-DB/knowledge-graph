package org.opensearch.graph.dispatcher.driver;




import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface DashboardDriver {

    ObjectNode graphElementCount(String ontology) ;

    ObjectNode graphElementCreated(String ontology) ;

    ObjectNode cursorCount();
}
