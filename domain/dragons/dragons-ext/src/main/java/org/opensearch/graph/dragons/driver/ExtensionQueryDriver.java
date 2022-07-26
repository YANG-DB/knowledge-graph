package org.opensearch.graph.dragons.driver;



import com.google.inject.Inject;
import org.opensearch.graph.core.driver.StandardQueryDriver;
import org.opensearch.graph.dispatcher.driver.CursorDriver;
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.dispatcher.epb.PlanSearcher;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryMetadata;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.transport.CreateJsonQueryRequest;
import org.opensearch.graph.model.transport.CreatePageRequest;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;

import java.util.Optional;
import java.util.UUID;

public class ExtensionQueryDriver extends StandardQueryDriver {

    public static final String TYPE_CLAUSE = "clause";

    //region Constructors
    @Inject
    public ExtensionQueryDriver(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            JsonQueryTransformerFactory transformerFactory,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        super(cursorDriver, pageDriver, queryTransformer, queryValidator, queryRewriter, transformerFactory, planSearcher, resourceStore, urlSupplier);
    }
    //endregion

    //region Implementation

    public Optional<QueryResourceInfo> createClause(CreateJsonQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = Optional.empty();
            if (request.getQueryType().equals(CreateJsonQueryRequest.TYPE_CYPHERQL)) {
                //support cypher type
                queryResourceInfo = this.create(request, metadata);
            } else if (request.getQueryType().equals(TYPE_CLAUSE)) {
                //support clause type
                Query query = transformer.transform(request.getQuery());
                queryResourceInfo = this.create(new CreateQueryRequest(request.getId(), request.getName(), query, request.getCreateCursorRequest()));
            }
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new GraphError(Query.class.getSimpleName(),
                            err.getMessage())));

        }
    }

    public Optional<Object> runClause(String clause, String ontology) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_CLAUSE, clause, ontology, new CreateGraphCursorRequest(new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);

            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
            delete(id);
        }
    }


        //endregion
    private QueryTransformer<String, Query> transformer;

}
