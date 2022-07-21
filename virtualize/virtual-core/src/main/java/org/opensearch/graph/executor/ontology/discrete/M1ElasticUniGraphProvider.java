package org.opensearch.graph.executor.ontology.discrete;


import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.controller.common.ElementController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingReduceController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchController;
import org.opensearch.graph.unipop.controller.common.logging.LoggingSearchVertexController;
import org.opensearch.graph.unipop.controller.discrete.*;
import org.opensearch.graph.unipop.controller.search.SearchOrderProviderFactory;
import org.opensearch.graph.unipop.process.traversal.strategy.FuseStandardStrategyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.structure.FuseUniGraph;
import org.opensearch.client.Client;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by Roman on 06/04/2017.
 */
public class M1ElasticUniGraphProvider implements UniGraphProvider {
    //region Constructors
    @Inject
    public M1ElasticUniGraphProvider(
            Client client,
            OpensearchGraphConfiguration opensearchGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProvider,
            MetricRegistry metricRegistry) {
        this.client = client;
        this.opensearchGraphConfiguration = opensearchGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.schemaProviderFactory = schemaProviderFactory;
        this.orderProvider = orderProvider;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        return new FuseUniGraph(
                this.uniGraphConfiguration,
                controllerManagerFactory(this.schemaProviderFactory.get(ontology), this.metricRegistry),
                new FuseStandardStrategyProvider());
    }

    //region Private Methods

    /**
     * default controller Manager
     *
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        return uniGraph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new DiscreteElementVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider,metricRegistry ),
                                        metricRegistry),
                                null
                        ),
                        new LoggingSearchVertexController(
                                new DiscreteVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider,
                                        metricRegistry),
                                metricRegistry),
                        new LoggingSearchVertexController(
                                new DiscreteVertexFilterController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider,
                                        metricRegistry),
                                metricRegistry),
                        new LoggingReduceController(
                                new DiscreteElementReduceController(client, opensearchGraphConfiguration, uniGraph, schemaProvider,
                                        metricRegistry),
                                metricRegistry)
                );
            }

            @Override
            public void close() {

            }
        };
    }
    //endregion

    //region Fields
    private final Client client;
    private final OpensearchGraphConfiguration opensearchGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    private SearchOrderProviderFactory orderProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
