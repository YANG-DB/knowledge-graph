package org.opensearch.graph.executor.ontology;


import org.opensearch.graph.model.ontology.Ontology;
import org.unipop.structure.UniGraph;

/**
 * Created by Roman on 06/04/2017.
 */
public interface UniGraphProvider {
    UniGraph getGraph(Ontology ontology) throws Exception;
}
