package org.openserach.graph.asg.translator.cypher;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.ontology.OntologyTransformerProvider;
import org.openserach.graph.asg.translator.cypher.strategies.*;
import org.openserach.graph.asg.translator.cypher.strategies.expressions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class M1CypherAsgStrategyRegistrar implements CypherAsgStrategyRegistrar {
    private final OntologyProvider ontologyProvider;
    private final OntologyTransformerProvider transformerProvider;

    //region Constructors
    @Inject
    public M1CypherAsgStrategyRegistrar(OntologyProvider ontologyProvider, OntologyTransformerProvider transformerProvider) {
        this.ontologyProvider = ontologyProvider;
        this.transformerProvider = transformerProvider;
    }

    //endregion

    @Override
    public Iterable<CypherTranslatorStrategy> register() {
        //translators
        translatorStrategies = Arrays.asList(
                new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                new StepPatternCypherTranslatorStrategy(
                        new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                        new EqualityExpression()
                )
        );

        //expressions
        whereExpressionStrategies = new ArrayList<>();
        whereExpressionStrategies.add(new OrExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new AndExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new HasLabelExpression());
        whereExpressionStrategies.add(new HasRelationLabelExpression());
        whereExpressionStrategies.add(new InequalityExpression());
        whereExpressionStrategies.add(new EqualityExpression());
        whereExpressionStrategies.add(new NotEqualExpression());
        whereExpressionStrategies.add(new StartsWithExpression());
        whereExpressionStrategies.add(new EndsWithExpression());
        whereExpressionStrategies.add(new InExpression());
        whereExpressionStrategies.add(new ContainsExpression());
        whereExpressionStrategies.add(new ContainsExpression());
        whereExpressionStrategies.add(new CountExpression());
        whereExpressionStrategies.add(new DistinctExpression());
        whereExpressionStrategies.add(new LikeExpression());

        returnExpressionStrategies = new ArrayList<>();
        returnExpressionStrategies.add(new DistinctExpression());
        returnExpressionStrategies.add(new ReturnVariableExpression());

        whereClause = new WhereClauseNodeCypherTranslator(whereExpressionStrategies);
        returnClause = new ReturnClauseNodeCypherTranslator(returnExpressionStrategies);
        match = new MatchCypherTranslatorStrategy(translatorStrategies, whereClause, returnClause);


        return Collections.singleton(match);
    }

    private List<CypherElementTranslatorStrategy> translatorStrategies;
    private List<ExpressionStrategies> whereExpressionStrategies;
    private List<ExpressionStrategies> returnExpressionStrategies;

    public MatchCypherTranslatorStrategy match;
    private WhereClauseNodeCypherTranslator whereClause;
    private ReturnClauseNodeCypherTranslator returnClause;
}
