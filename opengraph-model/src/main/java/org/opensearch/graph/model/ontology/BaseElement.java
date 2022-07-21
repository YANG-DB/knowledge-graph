package org.opensearch.graph.model.ontology;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opensearch.graph.model.GlobalConstants;

import java.util.List;
import java.util.StringJoiner;

/**
 * common attributed element shared by any properties enabled element
 */
public interface BaseElement {
    String getName();
    List<String> getIdField();
    List<String> getMetadata();
    List<String> fields();
    List<String> getProperties();


    @JsonIgnore
    static String idFieldName(List<String> values) {
        StringJoiner joiner = new StringJoiner("_");
        values.forEach(joiner::add);
        return joiner.toString().length() >0 ?
               joiner.toString() : GlobalConstants.ID;
    }
}
