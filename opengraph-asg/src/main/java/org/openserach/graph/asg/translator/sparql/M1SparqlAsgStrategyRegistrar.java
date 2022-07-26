package org.openserach.graph.asg.translator.sparql;






import com.google.inject.Inject;
import org.openserach.graph.asg.strategy.SparqlAsgStrategyRegistrar;
import org.openserach.graph.asg.translator.sparql.strategies.*;
import org.openserach.graph.asg.translator.sparql.strategies.expressions.CompareExpressionStrategy;
import org.openserach.graph.asg.translator.sparql.strategies.expressions.ExpressionStrategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class M1SparqlAsgStrategyRegistrar implements SparqlAsgStrategyRegistrar {

    //region Constructors
    @Inject
    public M1SparqlAsgStrategyRegistrar() {}

    //endregion

    @Override
    public Iterable<SparqlTranslatorStrategy> register() {
        //expressions
        whereExpressionStrategies.add(new CompareExpressionStrategy());

        //translators
        translatorStrategies.addAll(Arrays.asList(
                new ProjectionPatternTranslatorStrategy(
                        Arrays.asList(
                                new FilterPatternTranslatorStrategy(translatorStrategies, whereExpressionStrategies),
                                new ArbitraryPathTranslatorStrategy(translatorStrategies),
                                new JoinPatternTranslatorStrategy(translatorStrategies),
                                new UnionPatternTranslatorStrategy(translatorStrategies),
                                new NodePatternTranslatorStrategy())
                ),
                new FilterPatternTranslatorStrategy(translatorStrategies, whereExpressionStrategies),
                new ArbitraryPathTranslatorStrategy(translatorStrategies),
                new JoinPatternTranslatorStrategy(translatorStrategies),
                new UnionPatternTranslatorStrategy(translatorStrategies),
                new NodePatternTranslatorStrategy()
        ));

        return Collections.singleton(new RootTranslatorStrategy(translatorStrategies, whereExpressionStrategies));
    }

    private List<SparqlElementTranslatorStrategy> translatorStrategies = new ArrayList<>();
    private List<ExpressionStrategies> whereExpressionStrategies = new ArrayList<>();

}

