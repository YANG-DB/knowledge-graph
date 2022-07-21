package org.opensearch.graph.dispatcher.ontology;



public interface OntologyTransformerIfc<OntIn,OntOut> {
    OntOut transform(String ontologyName, OntIn source);
    OntIn translate(OntOut source);
}
