package org.opensearch.graph.epb.plan.statistics;


import org.opensearch.graph.model.ontology.Ontology;

/**
 * Created by Roman on 25/05/2017.
 */
public interface StatisticsProviderFactory {
    StatisticsProvider get(Ontology ontology);
}
