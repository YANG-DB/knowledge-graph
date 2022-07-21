package org.opensearch.graph.model.projection;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.GlobalConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Example:
 * {
 *         "nodes": [
 *             {
 *                 "id": "0",
*                  "label": "person",
 *                 "metadata": {
 *                     "user-defined": "values"
 *                 }
 *                 "properties":{
 *                     "fName": "first name",
 *                     "lName":"last name",
 *                     "born": "12/12/2000",
 *                     "age": "19",
 *                     "email": "myName@fuse.com",
 *                     "address": {
 *                             "state": "my state",
 *                             "street": "my street",
 *                             "city": "my city",
 *                             "zip": "gZip"
 *                     }
 *                 }
 *             },
 *             {
 *                 "id": "10",
 *                 "label": "person",
 *                 "metadata": {
 *                     "user-defined": "values"
 *                 }
 *                 "properties":{
 *                     "fName": "another first name",
 *                     "lName":"another last name",
 *                     "age": "20",
 *                     "born": "1/1/1999",
 *                     "email": "notMyName@fuse.com",
 *                     "address": {
 *                             "state": "not my state",
 *                             "street": "not my street",
 *                             "city": "not my city",
 *                             "zip": "not gZip"
 *                     }
 *                 }
 *             }
 *         ],
 *         "edges": [
 *             {
 *                 "id": 100,
 *                 "source": "0",
 *                 "target": "1",
 *                 "metadata": {
 *                     "label": "knows",
 *                     "user-defined": "values"
 *                 },
 *                 "properties":{
 *                      "date":"01/01/2000",
 *                      "medium": "facebook"
 *                 }
 *             },
 *             {
 *                 "id": 101,
 *                 "source": "0",
 *                 "target": "1",
 *                 "metadata": {
 *                     "label": "called",
 *                     "user-defined": "values"
 *                 },
 *                 "properties":{
 *                      "date":"01/01/2000",
 *                      "duration":"120",
 *                      "medium": "cellular"
 *                      "sourceLocation": "40.06,-71.34"
 *                      "sourceTarget": "41.12,-70.9"
 *                 }
 *             }
 *         ]
 *     }
 * */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectionAssignment {
    private List<ProjectionNode> nodes;
    private long id;
    private String queryId;
    private String cursorId;
    private long timestamp;

    public ProjectionAssignment(long id,String queryId,String cursorId,long timestamp) {
        this.id = id;
        this.queryId = queryId;
        this.cursorId = cursorId;
        this.timestamp = timestamp;
        this.nodes = new ArrayList<>();
    }

    public List<ProjectionNode> getNodes() {
        return nodes;
    }

    public long getId() {
        return id;
    }

    public String getQueryId() {
        return queryId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return GlobalConstants.ProjectionConfigs.PROJECTION;
    }

    public ProjectionAssignment with(ProjectionNode node) {
        getNodes().add(node);
        return this;
    }

    public ProjectionAssignment withAll(List<ProjectionNode> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public String getCursorId() {
        return cursorId;
    }
}
