package org.opensearch.graph.epb.plan.statistics;

/*-
 * #%L
 * virtual-epb
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





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
