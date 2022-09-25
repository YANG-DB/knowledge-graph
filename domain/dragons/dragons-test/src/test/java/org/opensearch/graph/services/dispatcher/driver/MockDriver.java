package org.opensearch.graph.services.dispatcher.driver;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.opensearch.graph.client.export.GraphWriterStrategy;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.driver.*;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.profile.QueryProfileInfo;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.QueryMetadata;
import org.opensearch.graph.model.results.QueryResultBase;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.ImmutableMetrics;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class MockDriver {
    public static class Query extends QueryDriverBase {
        //region Constructors
        @Inject
        public Query(
                CursorDriver cursorDriver,
                PageDriver pageDriver,
                QueryTransformer<org.opensearch.graph.model.query.Query, AsgQuery> queryTransformer,
                JsonQueryTransformerFactory transformerFactory,
                QueryValidator<AsgQuery> queryValidator,
                ResourceStore resourceStore,
                AppUrlSupplier urlSupplier) {
            super(cursorDriver, pageDriver, queryTransformer, transformerFactory, queryValidator, resourceStore, urlSupplier);
        }
        //endregion

        //region QueryDriverBase Implementation
        @Override
        protected QueryResource createResource(CreateQueryRequest request, org.opensearch.graph.model.query.Query query, AsgQuery asgQuery, QueryMetadata metadata) {
            return new QueryResource(request, query, asgQuery, metadata, new PlanWithCost<>(new Plan(), new PlanDetailedCost()), Optional.empty());
        }

        @Override
        protected AsgQuery rewrite(AsgQuery asgQuery) {
            return asgQuery;
        }

        @Override
        public Optional<Object> run(org.opensearch.graph.model.query.Query query, int pageSize, String cursorType) {
            return Optional.empty();
        }

        @Override
        public Optional<Object> runCypher(String cypher, String ontology) {
            return Optional.empty();
        }

        @Override
        public Optional<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
            return Optional.empty();
        }

        @Override
        public Optional<GraphTraversal> traversal(org.opensearch.graph.model.query.Query query) {
            return Optional.empty();
        }

        @Override
        protected PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query) {
            return PlanWithCost.EMPTY_PLAN;
        }
        //endregion
    }

    public static class Cursor extends CursorDriverBase {
        //region Constructors
        @Inject
        public Cursor(MetricRegistry registry,ResourceStore resourceStore, PageDriver pageDriver,
                      AppUrlSupplier urlSupplier, CursorFactory cursorFactory) {
            super(registry,resourceStore, urlSupplier);
            this.pageDriver = pageDriver;
            this.cursorFactory = cursorFactory;
        }
        //endregion

        //region CursorDriverBase Implementation
        @Override
        protected CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest) {
            return new CursorResource(
                    cursorId,
                    cursorFactory.createCursor(
                            new CursorFactory.Context.Impl<>(
                                    new Object(),
                                    new OntologyProvider() {
                                        @Override
                                        public Optional<Ontology> get(String id) {
                                            return Optional.empty();
                                        }

                                        @Override
                                        public Collection<Ontology> getAll() {
                                            return null;
                                        }

                                        @Override
                                        public Ontology add(Ontology ontology) {
                                            return null;
                                        }
                                    },
                                    queryResource,
                                    cursorRequest)),
                    new QueryProfileInfo.QueryProfileInfoImpl(new ImmutableMetrics() {
                    }),
                    cursorRequest);
        }
        //endregion

        //region Fields
        private CursorFactory cursorFactory;


        @Override
        public Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology) {
            return Optional.empty();
        }
        //endregion
    }

    public static class Page extends PageDriverBase {
        //region Constructors
        @Inject
        public Page(ResourceStore resourceStore, AppUrlSupplier urlSupplier, GraphWriterStrategy writerMap) {
            super(resourceStore, urlSupplier, writerMap);
        }
        //endregion

        //region PageDriverBase Implementation
        @Override
        protected PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
            QueryResultBase assignmentsQueryResult = cursorResource.getCursor().getNextResults(pageSize);
            return new PageResource<>(pageId, assignmentsQueryResult, pageSize, 0);
        }

        //endregion
    }
}
