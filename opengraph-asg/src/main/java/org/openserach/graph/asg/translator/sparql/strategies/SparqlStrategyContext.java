package org.openserach.graph.asg.translator.sparql.strategies;



import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.HashMap;
import java.util.Map;

public class SparqlStrategyContext {

    public SparqlStrategyContext(Ontology ontology, ParsedQuery statement, AsgQuery query,AsgEBase<? extends EBase> scope) {
        this.ontology = new Ontology.Accessor(ontology);
        this.statement = statement;
        this.query = query;
        this.scope = scope;
        this.anonymLabels = new HashMap<>();
    }

    public AsgEBase<? extends EBase> getScope() {
        return scope;
    }

    public ParsedQuery getStatement() {
        return statement;
    }

    public Ontology.Accessor getOntology() {
        return ontology;
    }

    public SparqlStrategyContext scope(AsgEBase<? extends EBase> scope) {
        this.scope = scope;
        return this;
    }

    public AsgQuery getQuery() {
        return query;
    }

    private Ontology.Accessor ontology;
    //region Fields
    private ParsedQuery statement;

    private AsgQuery query;
    private AsgEBase<? extends EBase> scope;
    private Map<String,String> anonymLabels;

    //endregion
}
