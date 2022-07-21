package org.opensearch.graph.dispatcher.driver;




import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.StoreResourceInfo;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface CursorDriver {
    /**
     * create cursor resource
     * @param queryId
     * @param cursorRequest
     * @return
     */
    Optional<CursorResourceInfo> create(String queryId, CreateCursorRequest cursorRequest);

    /**
     * get cursors resource info according ot query Id
     * @param queryId
     * @return
     */
    Optional<StoreResourceInfo> getInfo(String queryId);

    /**
     * get specific cursor resource info according to the cursor & query ids
     * @param queryId
     * @param cursorId
     * @return
     */
    Optional<CursorResourceInfo> getInfo(String queryId, String cursorId);

    /**
     * get the graph traversal physical plan according to the logical plan
     * @param plan
     * @param ontology
     * @return
     */
    Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology);

    /**
     * delete the cursor resource and its related sub-resources
     * @param queryId
     * @param cursorId
     * @return
     */
    Optional<Boolean> delete(String queryId, String cursorId);
}
