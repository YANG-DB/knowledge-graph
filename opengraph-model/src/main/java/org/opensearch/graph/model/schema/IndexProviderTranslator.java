package org.opensearch.graph.model.schema;





public interface IndexProviderTranslator<IN> {
    IndexProvider translate(String ontology, IN input);
}
