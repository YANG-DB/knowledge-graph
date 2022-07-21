package org.opensearch.graph.model.profile;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Query profiling step info
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryProfileStepInfoData {
    private String stepName;
    private long stepCount;
    private long batchSize;
    private String stepQuery;

    public QueryProfileStepInfoData() {}

    public QueryProfileStepInfoData(String stepName, long stepCount, String stepQuery) {
        this.stepName = stepName;
        this.stepCount = stepCount;
        this.stepQuery = stepQuery;
    }

    public QueryProfileStepInfoData(String stepName, long stepCount,long batchSize, String stepQuery) {
        this.stepName = stepName;
        this.stepCount = stepCount;
        this.batchSize = batchSize;
        this.stepQuery = stepQuery;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setStepQuery(String stepQuery) {
        this.stepQuery = stepQuery;
    }

    public String getStepName() {
        return stepName;
    }

    public String getStepQuery() {
        return stepQuery;
    }

    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public long getStepCount() {
        return stepCount;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }
}
