package org.opensearch.graph.test.estimator;



import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.OptionalDouble;

/**
 * Created by lior.perry on 27/02/2018.
 */
public class OntologyConfigurationRuleBasedStatisticalProvider implements StatisticsProviderFactory {
    public static final String OPERATORS = "operators";
    public static final String NODES = "nodes";
    public static final String EDGES = "edges";
    public static final String SELECTIVITY = "selectivity";
    public static final String TOTAL = "total";
    private Map<String, Object> map;

    public OntologyConfigurationRuleBasedStatisticalProvider() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("./assembly/Dragons/rules/setup.json");
        map = new ObjectMapper().readValue(stream, Map.class);
    }

    @Override
    public StatisticsProvider get(Ontology ontology) {
        return new RuleBasedStatisticalProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                Map node = getNode(map, item);
                if (item instanceof EConcrete) {
                    int selectivity = 1;
                    if (!node.isEmpty()) {
                        selectivity = (Integer) node.get(SELECTIVITY);
                    }
                    return new Statistics.SummaryStatistics(selectivity, selectivity);
                } else if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics((Integer) node.get(TOTAL) * (Integer) node.get(SELECTIVITY), (Integer) node.get(TOTAL) * (Integer) node.get(SELECTIVITY));
                else if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(Integer.MAX_VALUE, Integer.MAX_VALUE);

                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                Statistics.SummaryStatistics nodeStatistics = getNodeStatistics(item);
                OptionalDouble max = entityFilter.getProps().stream().mapToDouble(f -> {
                    String property = f.getpType();
                    int propCardinality = (int) getNode(map, item).get(property);
                    ConstraintOp op = f.getCon().getOp();
                    return getOperatorAlpha(map, op) * propCardinality;
                }).max();
                double maxValue = max.orElse(1);
                return new Statistics.SummaryStatistics(nodeStatistics.getTotal() / maxValue, nodeStatistics.getCardinality() / maxValue);
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


    public static Map getNode(Map map, EEntityBase item) {
        return (Map) ((Map) map.get(NODES)).getOrDefault(((ETyped) item).geteType(), Collections.emptyMap());
    }

    public static double getOperatorAlpha(Map map, ConstraintOp op) {
        return (double) ((Map) map.get(OPERATORS)).getOrDefault(op.toString(), 1.0d);
    }

    public static Map getEdge(Map map, Rel item) {
        return (Map) ((Map) map.get(EDGES)).getOrDefault(item.getrType(), Collections.emptyMap());
    }
}
