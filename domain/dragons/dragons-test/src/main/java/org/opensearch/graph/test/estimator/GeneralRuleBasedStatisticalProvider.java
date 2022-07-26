package org.opensearch.graph.test.estimator;



import org.opensearch.graph.epb.plan.statistics.RuleBasedStatisticalProvider;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.epb.plan.statistics.StatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.StatisticsProviderFactory;
import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;

import java.util.OptionalDouble;

/**
 * Created by lior.perry on 27/02/2018.
 */
public class GeneralRuleBasedStatisticalProvider implements StatisticsProviderFactory {
    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new RuleBasedStatisticalProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                if (item instanceof EConcrete)
                    return new Statistics.SummaryStatistics(1, 1);
                else if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics(100, 100);
                else if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(Integer.MAX_VALUE, Integer.MAX_VALUE);

                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                Statistics.SummaryStatistics nodeStatistics = getNodeStatistics(item);
                OptionalDouble max = entityFilter.getProps().stream().mapToDouble(f -> {
                    ConstraintOp op = f.getCon().getOp();
                    switch (op) {
                        case contains:
                            return 30;
                        case eq:
                            return 80;
                        case like:
                            return 10;
                        case empty:
                            return 20;
                        case inSet:
                            return 60;
                    }
                    return 1;
                }).max();
                double maxValue = max.orElse(1);
                return new Statistics.SummaryStatistics(nodeStatistics.getTotal()/maxValue,nodeStatistics.getCardinality()/maxValue);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                return 1;
            }
        };
    }
}
