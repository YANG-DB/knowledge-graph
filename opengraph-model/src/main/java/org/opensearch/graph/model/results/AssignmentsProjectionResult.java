package org.opensearch.graph.model.results;

/*-
 * #%L
 * opengraph-model
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.logical.Edge;
import org.opensearch.graph.model.logical.Vertex;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.FuseError;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsProjectionResult<E extends Vertex,R extends Edge> extends AssignmentsQueryResult<E,R> {
    //region Constructors
    public AssignmentsProjectionResult() {
        this.assignments = Collections.emptyList();
    }

    public AssignmentsProjectionResult(List<Assignment<E,R>> assignments) {
        this.assignments = assignments;
    }

    public AssignmentsProjectionResult(LoadResponse<String, FuseError> load) {
        this.assignments.add(LoadResponse.buildAssignment(load));
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
        return "AssignmentsProjectionResult [pattern = "+pattern+", assignments = "+assignments+"]";
    }
    //endregion

    //region Fields
    private Query pattern;
    private List<Assignment<E,R>> assignments = new ArrayList<>();

    @Override
    public int getSize() {
        return this.getAssignments().size();
    }

    @Override
    public String content() {
        return AssignmentQueryResultsDescriptor.print((AssignmentsProjectionResult<Entity, Relationship>) this);
    }
    //endregion



    /**
     * remove assignments duplicates
     * @param assignmentsQueryResult
     * @return
     */
    public static AssignmentsProjectionResult<Entity,Relationship> distinct(AssignmentsProjectionResult<Entity,Relationship> assignmentsQueryResult) {
        assignmentsQueryResult.setAssignments(Stream.ofAll(assignmentsQueryResult.getAssignments())
                .distinctBy(AssignmentDescriptor::print)
                .toJavaList());
        return assignmentsQueryResult;

    }


}
