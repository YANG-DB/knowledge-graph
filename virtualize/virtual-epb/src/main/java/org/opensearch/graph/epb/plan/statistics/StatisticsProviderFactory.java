package org.opensearch.graph.epb.plan.statistics;





import org.opensearch.graph.model.ontology.Ontology;

public interface StatisticsProviderFactory {
    StatisticsProvider get(Ontology ontology);
}
