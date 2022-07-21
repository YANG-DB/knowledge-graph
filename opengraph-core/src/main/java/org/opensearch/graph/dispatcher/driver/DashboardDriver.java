package org.opensearch.graph.dispatcher.driver;




import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public interface DashboardDriver {

    ObjectNode graphElementCount(String ontology) ;

    ObjectNode graphElementCreated(String ontology) ;

    ObjectNode cursorCount();
}
