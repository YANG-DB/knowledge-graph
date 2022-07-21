package org.opensearch.graph.core.driver;


import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.driver.CursorDriverBase;
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.provision.CursorRuntimeProvision;
import org.opensearch.graph.dispatcher.profile.QueryProfileInfo;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.executor.CompositeTraversalCursorContext;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreateInnerQueryCursorRequest;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.client.Client;
import org.unipop.process.Profiler;
import org.unipop.process.Profiler.Noop;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static org.unipop.process.Profiler.PROFILER;

public class StandardCursorDriver extends CursorDriverBase {
    private final Client client;
    private final GraphElementSchemaProviderFactory schemaProvider;

    //region Constructors
    @Inject
    public StandardCursorDriver(
            Client client,
            ResourceStore resourceStore,
            PageDriver pageDriver,
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider,
            AppUrlSupplier urlSupplier,
            MetricRegistry registry) {
        super(registry, resourceStore, urlSupplier);
        this.client = client;
        this.schemaProvider = schemaProviderFactory;
        this.pageDriver = pageDriver;
        this.ontologyProvider = ontologyProvider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.cursorFactory = cursorFactory;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region CursorDriverBase Implementation
    @Override
    protected CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest) {
        PlanWithCost<Plan, PlanDetailedCost> executionPlan = queryResource.getExecutionPlan();
        //get the ontology name from the asg query since a transformation between ontologies might have occurred - see AsgMappingStrategy
        Ontology ontology = this.ontologyProvider.get(queryResource.getAsgQuery().getOnt())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology field found for " + queryResource.getAsgQuery().getOnt())));

        GraphTraversal<?, ?> traversal = createTraversal(executionPlan, ontology);

        //execution context
        String prefix = String.format("%s.%s", queryResource.getQueryMetadata().getId(), cursorId);
        traversal.asAdmin().getSideEffects().register(CONTEXT, () -> prefix, null);

        //default no operation profiler - no memory footprint or activity
        traversal.asAdmin().getSideEffects().register(PROFILER, () -> Noop.instance, null);

//      activate profiling configuration - override noop default profiler with a real one - large memory footprint - caution
        if (cursorRequest.isProfile()) {
            traversal.asAdmin().getSideEffects().register(PROFILER, Profiler.Impl::new, null);
        }

        //todo in case of composite cursor -> add depended cursors for query
        //if query has inner queries -> create new CreateInnerQueryCursorRequest(cursorRequest)
        TraversalCursorContext context = createContext(queryResource, cursorRequest, cursorId, ontology, traversal);
        Cursor cursor = this.cursorFactory.createCursor(context);
        Profiler profiler = traversal.asAdmin().getSideEffects().get(PROFILER);

        return new CursorResource(cursorId, cursor, new QueryProfileInfo.QueryProfileInfoImpl(profiler.get()), cursorRequest);
    }

    protected TraversalCursorContext createContext(QueryResource queryResource, CreateCursorRequest cursorRequest, String cursorId, Ontology ontology, GraphTraversal<?, ?> traversal) {
        String prefix = String.format("%s.%s", queryResource.getQueryMetadata().getId(), cursorId);
        TraversalCursorContext context = new TraversalCursorContext(
                client,
                schemaProvider.get(ontology),
                ontologyProvider,
                ontology,
                queryResource,
                cursorRequest,
                new CursorRuntimeProvision.MetricRegistryCursorRuntimeProvision(prefix, registry),
                traversal.path());
        if (hasInnerQuery(queryResource.getAsgQuery())) {
            List<QueryResource> queryResources = Stream.ofAll(queryResource.getInnerQueryResources()).toJavaList();
            //first level (hierarchy) inner queries
            return new CompositeTraversalCursorContext(
                    new TraversalCursorContext(
                            client,
                            schemaProvider.get(ontology),
                            ontologyProvider,
                            ontology,
                            queryResource,
                            new CreateInnerQueryCursorRequest(cursorRequest),
                            new CursorRuntimeProvision.MetricRegistryCursorRuntimeProvision(prefix, registry),
                            traversal.path()), queryResources);
        }
        return context;
    }

    protected GraphTraversal<?, ?> createTraversal(PlanWithCost<Plan, PlanDetailedCost> plan, Ontology ontology) {
        try {
            return this.planTraversalTranslator.translate(
                    plan,
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology) {
        return Optional.of(createTraversal(plan, this.ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology found for " + ontology)))));
    }

    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;


    //endregion
}
