package org.opensearch.graph.executor.ontology;





import org.opensearch.graph.model.ontology.Ontology;
import org.unipop.structure.UniGraph;

public interface UniGraphProvider {
    UniGraph getGraph(Ontology ontology) throws Exception;
}
