package org.openserach.graph.asg.translator.sparql;


import com.google.inject.Inject;
import org.openserach.graph.asg.strategy.SparqlAsgStrategyRegistrar;
import org.openserach.graph.asg.translator.AsgTranslator;
import org.openserach.graph.asg.translator.sparql.strategies.SparqlStrategyContext;
import org.openserach.graph.asg.translator.sparql.strategies.SparqlTranslatorStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.semanticweb.owlapi.model.IRI;

public class SparqlTranslator implements AsgTranslator<QueryInfo<String>, AsgQuery> {

    @Inject
    public SparqlTranslator(OntologyProvider provider,SparqlAsgStrategyRegistrar strategies) {
        this.provider = provider;
        this.strategies = strategies.register();
    }
    //endregion


    @Override
    public AsgQuery translate(QueryInfo<String> source) {
        Ontology ontology = provider.get(source.getOntology())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for Id ", "No Ontology present for id[" + source.getOntology()+"]")));

        final AsgQuery query = AsgQuery.Builder.start("sparql_", source.getOntology()).build();

        //translate cypher asci into cypher AST
        ParsedQuery statement = new SPARQLParser().parseQuery(source.getQuery(), IRI.create(query.getOnt()).toString());
        final SparqlStrategyContext context = new SparqlStrategyContext(ontology,statement,query, query.getStart());

        //todo implement projection fields
//        query.setProjectedFields(populate);

        //apply strategies
        strategies.iterator().forEachRemaining(strategy -> strategy.apply(query, context));
        return query;
    }

    private OntologyProvider provider;
    private Iterable<SparqlTranslatorStrategy> strategies;

}
