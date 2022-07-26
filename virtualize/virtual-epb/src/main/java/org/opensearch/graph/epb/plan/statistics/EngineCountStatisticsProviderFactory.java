package org.opensearch.graph.epb.plan.statistics;





import com.google.inject.Provider;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.epb.plan.statistics.configuration.EngineCountStatisticsConfig;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.ontology.Ontology;
import com.typesafe.config.Config;

import javax.inject.Inject;

public class EngineCountStatisticsProviderFactory implements StatisticsProviderFactory {
    private PlanTraversalTranslator planTraversalTranslator;
    private Provider<UniGraphProvider> uniGraphProvider;
    private EngineCountStatisticsConfig engineCountStatisticsConfig;

    @Inject
    public EngineCountStatisticsProviderFactory(PlanTraversalTranslator planTraversalTranslator, Provider<UniGraphProvider> uniGraphProvider, Config config) {
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
        engineCountStatisticsConfig = new EngineCountStatisticsConfig(config);
    }

    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new EngineCountStatisticsProvider(planTraversalTranslator, ontology, uniGraphProvider, engineCountStatisticsConfig);
    }
}
