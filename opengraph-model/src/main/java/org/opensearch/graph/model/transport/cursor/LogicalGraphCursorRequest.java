package org.opensearch.graph.model.transport.cursor;



import org.opensearch.graph.model.transport.CreatePageRequest;

import java.util.Collections;

public class LogicalGraphCursorRequest extends CreateGraphHierarchyCursorRequest {
    public static final String CursorType = "LogicalGraphCursorRequest";
    public String ontology;

    //region Constructors
    public LogicalGraphCursorRequest() {
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology) {
        super();
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags) {
        super(countTags);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology, CreatePageRequest createPageRequest) {
        super(Collections.emptyList(),createPageRequest);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(ontology,countTags,createPageRequest, GraphFormat.JSON);
        this.ontology = ontology;
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags, CreatePageRequest createPageRequest, GraphFormat format ) {
        super(countTags, createPageRequest);
        this.setCursorType(CursorType);
        this.ontology = ontology;
    }

    public LogicalGraphCursorRequest(String ontology,Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(ontology,include,countTags,createPageRequest, GraphFormat.JSON);
    }

    public LogicalGraphCursorRequest(String ontology,Include include, Iterable<String> countTags, CreatePageRequest createPageRequest, GraphFormat format) {
        super(include, countTags, createPageRequest,format);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public String getOntology() {
        return ontology;
    }
}
