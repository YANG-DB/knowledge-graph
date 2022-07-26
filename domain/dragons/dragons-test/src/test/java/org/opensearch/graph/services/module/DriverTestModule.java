package org.opensearch.graph.services.module;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.driver.CursorDriver;
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.dispatcher.driver.QueryDriver;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import org.opensearch.graph.dispatcher.resource.store.InMemoryResourceStore;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.executor.cursor.discrete.GraphQLTraversalCursor;
import org.opensearch.graph.executor.cursor.discrete.GraphTraversalCursor;
import org.opensearch.graph.executor.cursor.discrete.PathsTraversalCursor;
import org.opensearch.graph.executor.mock.opensearch.MockClient;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.schema.load.CSVDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.executor.ontology.schema.load.GraphInitiator;
import org.opensearch.graph.executor.ontology.schema.load.VoidGraphInitiator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.transport.cursor.CreateGraphCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreateGraphQLCursorRequest;
import org.opensearch.graph.model.transport.cursor.CreatePathsCursorRequest;
import org.opensearch.graph.services.dispatcher.driver.MockDriver;
import org.opensearch.graph.services.engine2.data.schema.InitialTestDataLoader;
import org.opensearch.graph.services.engine2.data.schema.discrete.M2DragonsPhysicalSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.OntologySchemaProvider;
import org.opensearch.client.Client;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

/**
 * Created by Roman on 04/04/2017.
 */
public class DriverTestModule extends ModuleBase {
    @Override
    public void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        binder.bind(JsonQueryTransformerFactory.class).toInstance(type -> query -> AsgQuery.AsgQueryBuilder.anAsgQuery().build());
        binder.bind(ResourceStore.class).toInstance(new InMemoryResourceStore());
        binder.bind(QueryDriver.class).to(MockDriver.Query.class).in(RequestScoped.class);
        binder.bind(CursorDriver.class).to(MockDriver.Cursor.class).in(RequestScoped.class);
        binder.bind(PageDriver.class).to(MockDriver.Page.class).in(RequestScoped.class);

        binder.bind(GraphElementSchemaProviderFactory.class)
                .toInstance(ontology -> new OntologySchemaProvider(ontology, new M2DragonsPhysicalSchemaProvider()));
        binder.bind(Client.class).toInstance(new MockClient());

        InitialTestDataLoader loader = new InitialTestDataLoader(null, null);
        binder.bind(GraphDataLoader.class).toInstance(loader);
        binder.bind(CSVDataLoader.class).toInstance(loader);
        binder.bind(GraphInitiator.class).toInstance(new VoidGraphInitiator());

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new PathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreateGraphCursorRequest.CursorType,
                CreateGraphCursorRequest.class,
                new GraphTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreateGraphQLCursorRequest.CursorType,
                CreateGraphQLCursorRequest.class,
                new GraphQLTraversalCursor.Factory()));
    }

}
