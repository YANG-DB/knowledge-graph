package org.openserach.graph.asg.translator.cypher.strategies;



import org.openserach.graph.asg.translator.cypher.strategies.expressions.ReturnClauseNodeCypherTranslator;
import org.openserach.graph.asg.translator.cypher.strategies.expressions.WhereClauseNodeCypherTranslator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.PatternElement;
import org.opencypher.v9_0.expressions.PatternPart;
import scala.collection.Seq;

import java.util.Collection;
import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class MatchCypherTranslatorStrategy implements CypherTranslatorStrategy {

    public MatchCypherTranslatorStrategy(Iterable<CypherElementTranslatorStrategy> strategies, WhereClauseNodeCypherTranslator whereClause, ReturnClauseNodeCypherTranslator returnClause) {
        this.strategies = strategies;
        this.whereClause = whereClause;
        this.returnClause = returnClause;
    }

    @Override
    public void apply(AsgQuery query, CypherStrategyContext context) {
        final Statement statement = context.getStatement();
        if (!(statement instanceof Query)) return;

        Query cypherQuery = (Query) statement;
        final QueryPart part = cypherQuery.part();
        if (part instanceof SingleQuery) {
            manageMatchStatement(query, context, (SingleQuery) part);
            manageReturnStatement(query, context, (SingleQuery) part);
        }
    }

    private void manageReturnStatement(AsgQuery query, CypherStrategyContext context, SingleQuery part) {
        final Seq<Clause> clauses = part.clauses();
        final Optional<Clause> clause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Return.class)).findAny();

        if (!clause.isPresent()) return;

        Return aReturn = (Return) clause.get();
        // handle return aliases and functions (distinct / groupBy )
        returnClause.apply(aReturn, query, context);
    }

    private void manageMatchStatement(AsgQuery query, CypherStrategyContext context, SingleQuery part) {
        final Seq<Clause> clauses = part.clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        if (!matchClause.isPresent()) return;

        //manage patterns
        final Match match = (Match) matchClause.get();

        if (!match.where().isEmpty()) {
            context.where(match.where().get());
        }

        final Collection<PatternPart> patternParts = asJavaCollectionConverter(match.pattern().patternParts()).asJavaCollection();
        //for multi patterns match clause
        patternParts.forEach(p -> applyPattern(p.element(), context, query));

        //manage where clause
        if (!match.where().isEmpty()) {
            Where where = match.where().get();
            whereClause.apply(where, query, context);
        }
    }

    protected void applyPattern(PatternElement patternPart, CypherStrategyContext context, AsgQuery query) {
        strategies.forEach(s -> s.apply(patternPart, query, context));
    }

    private Iterable<CypherElementTranslatorStrategy> strategies;
    private WhereClauseNodeCypherTranslator whereClause;
    private ReturnClauseNodeCypherTranslator returnClause;
}
