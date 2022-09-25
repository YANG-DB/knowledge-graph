package org.opensearch.graph.services.test;

import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.client.GraphClient;
import org.junit.Assert;

public abstract class TestCase {

    public abstract void run(GraphClient GraphClient) throws Exception;


    protected void testAndAssertQuery(Query query, GraphClient GraphClient) throws Exception {
        long start = System.currentTimeMillis();
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        long queryStart = System.currentTimeMillis();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        long queryEnd = System.currentTimeMillis();
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 100000);
        Plan actualPlan = GraphClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());

        long end = System.currentTimeMillis();

//        System.out.println(actualPlan);
        System.out.println("Total time: " + (end -start));
        totalTime = end-start;
        planTime = queryEnd - queryStart;
        assignments = actualAssignmentsQueryResult.getAssignments().size();
        Assert.assertNotNull(actualAssignmentsQueryResult);
//        System.out.println("Assignments: " + assignments);
//        System.out.println(actualAssignmentsQueryResult);
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getPlanTime() {
        return planTime;
    }

    public long getAssignments() {
        return assignments;
    }

    private long totalTime;
    private long planTime;
    private long assignments;

}
