package org.openserach.graph.asg.translator.cypher;





import com.google.inject.Inject;
import org.openserach.graph.asg.translator.AsgTranslator;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherTranslatorStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.QueryInfo;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;

public class CypherTranslator implements AsgTranslator<QueryInfo<String>, AsgQuery> {

    @Inject
    public CypherTranslator(CypherAsgStrategyRegistrar strategies) {
        this.strategies = strategies.register();
    }
    //endregion


    @Override
    public AsgQuery translate(QueryInfo<String> source) {

        final AsgQuery query = AsgQuery.Builder.start("cypher_", source.getOntology()).build();

        //translate cypher asci into cypher AST
        final Statement statement = new CypherParser().parse(source.getQuery(), Option.empty());
        final CypherStrategyContext context = new CypherStrategyContext(statement, query.getStart());


        //apply strategies
        strategies.iterator().forEachRemaining(cypherTranslatorStrategy -> cypherTranslatorStrategy.apply(query, context));

        //collect projection fields
        query.setProjectedFields(AsgQueryUtil.groupByTags(query.getStart()));
        return query;
    }

    private Iterable<CypherTranslatorStrategy> strategies;
}
