package org.opensearch.graph.model.results;



import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by benishue on 21-Feb-17.
 */


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentCount extends Assignment{
    //region Constructors
    public AssignmentCount(Map<String, AtomicLong> labelsCount) {
        this.labelsCount = labelsCount;
    }
    //endregion

    //region Properties

    @JsonAnyGetter
    public Map<String, AtomicLong> getLabelsCount() {
        return labelsCount;
    }


    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "Assignment ["+labelsCount.toString()+"]";
    }
    //endregion

    //region Fields
    private Map<String, AtomicLong> labelsCount ;
    //endregion

}
