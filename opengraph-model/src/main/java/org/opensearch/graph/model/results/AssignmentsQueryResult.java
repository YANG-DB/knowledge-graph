package org.opensearch.graph.model.results;







import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.logical.Edge;
import org.opensearch.graph.model.logical.Vertex;
import org.opensearch.graph.model.query.Query;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsQueryResult<E extends Vertex,R extends Edge> extends QueryResultBase implements TextContent {
    //region Constructors
    public AssignmentsQueryResult() {
        this.assignments = Collections.emptyList();
    }

    public AssignmentsQueryResult(List<Assignment<E,R>> assignments) {
        this.assignments = assignments;
    }
    //endregion

    //region Properties
    public Query getPattern ()
    {
        return pattern;
    }

    public String getResultType(){
        return "assignments";
    }

    public void setPattern (Query pattern)
    {
        this.pattern = pattern;
    }

    public List<Assignment<E,R>> getAssignments ()
    {
        return assignments;
    }

    public void setAssignments (List<Assignment<E,R>> assignments)
    {
        this.assignments = assignments;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "AssignmentsQueryResult [pattern = "+pattern+", assignments = "+assignments+"]";
    }
    //endregion

    //region Fields
    private Query pattern;
    private List<Assignment<E,R>> assignments;

    @Override
    public int getSize() {
        return this.getAssignments().size();
    }

    @Override
    public String content() {
        return AssignmentQueryResultsDescriptor.print((AssignmentsQueryResult<Entity, Relationship>) this);
    }
    //endregion

    public static final class Builder<E extends Vertex,R extends Edge> {
        //region Constructors
        private Builder() {
            assignments = new ArrayList<>();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withPattern(Query pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withQueryId(String queryId) {
            this.queryId = queryId;
            return this;
        }

        public Builder withCursorId(String cursorId) {
            this.cursorId = cursorId;
            return this;
        }

        public Builder withAssignment(Assignment<E,R> assignments) {
            this.assignments.add(assignments);
            return this;
        }

        public Builder withAssignments(List<Assignment<E,R>> assignments) {
            this.assignments = assignments;
            return this;
        }

        public AssignmentsQueryResult<E,R> build() {
            AssignmentsQueryResult<E,R> assignmentsQueryResult = new AssignmentsQueryResult<>();
            assignmentsQueryResult.setPattern(pattern);
            assignmentsQueryResult.setAssignments(assignments);
            assignmentsQueryResult.setQueryId(queryId);
            assignmentsQueryResult.setCursorId(cursorId);
            assignmentsQueryResult.setTimestamp(timestamp);
            return assignmentsQueryResult;
        }
        //endregion

        //region Fields
        private Query pattern;
        private String queryId;
        private String cursorId;
        private long timestamp;
        private List<Assignment<E,R>> assignments;
        //endregion
    }

    /**
     * remove assignments duplicates
     * @param assignmentsQueryResult
     * @return
     */
    public static AssignmentsQueryResult<Entity,Relationship> distinct(AssignmentsQueryResult<Entity,Relationship> assignmentsQueryResult) {
        assignmentsQueryResult.setAssignments(Stream.ofAll(assignmentsQueryResult.getAssignments())
                .distinctBy(AssignmentDescriptor::print)
                .toJavaList());
        return assignmentsQueryResult;

    }


}
