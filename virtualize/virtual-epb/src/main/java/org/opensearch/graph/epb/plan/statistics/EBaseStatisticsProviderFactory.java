package org.opensearch.graph.epb.plan.statistics;



import com.google.inject.Inject;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;

public class EBaseStatisticsProviderFactory implements StatisticsProviderFactory {
    //region Constructor
    @Inject
    public EBaseStatisticsProviderFactory(
            GraphElementSchemaProviderFactory graphElementSchemaProviderFactory,
            GraphStatisticsProvider graphStatisticsProvider) {
        this.graphElementSchemaProviderFactory = graphElementSchemaProviderFactory;
        this.graphStatisticsProvider = graphStatisticsProvider;
    }
    //endregion

    //region StatisticsProviderFactory Implementation
    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new EBaseStatisticsProvider(
                this.graphElementSchemaProviderFactory.get(ontology),
                new Ontology.Accessor(ontology),
                graphStatisticsProvider);
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory graphElementSchemaProviderFactory;
    private GraphStatisticsProvider graphStatisticsProvider;
    //endregion
}
