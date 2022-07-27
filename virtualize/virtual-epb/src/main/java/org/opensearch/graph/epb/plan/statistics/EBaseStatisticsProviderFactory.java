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
