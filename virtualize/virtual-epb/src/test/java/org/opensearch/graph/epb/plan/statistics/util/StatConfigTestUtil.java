package org.opensearch.graph.epb.plan.statistics.util;

import org.opensearch.graph.epb.plan.statistics.configuration.StatConfig;
import org.opensearch.graph.stats.model.configuration.StatContainer;

import java.util.Collections;

/**
 * Created by benishue on 29-May-17.
 */
public class StatConfigTestUtil {

    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TERM_TYPE_NAME = "bucketTerm";
    private static final String STAT_STRING_TYPE_NAME = "bucketString";
    private static final String STAT_NUMERIC_TYPE_NAME = "bucketNumeric";
    private static final String STAT_GLOBAL_TYPE_NAME = "bucketGlobal";
    private static final String STAT_COUNT_FIELD_NAME = "count";
    private static final String STAT_CARDINALITY_FIELD_NAME = "cardinality";
    private static final String STAT_FIELD_TERM_NAME = "term";
    private static final String STAT_FIELD_NUMERIC_DOUBLE_LOWER_NAME = "lower_bound_numericDouble";
    private static final String STAT_FIELD_NUMERIC_DOUBLE_UPPER_NAME = "upper_bound_numericDouble";
    private static final String STAT_FIELD_NUMERIC_LONG_LOWER_NAME = "lower_bound_numericLong";
    private static final String STAT_FIELD_NUMERIC_LONG_UPPER_NAME = "upper_bound_numericLong";
    private static final String STAT_FIELD_STRING_LOWER_NAME = "lower_bound_string";
    private static final String STAT_FIELD_STRING_UPPER_NAME = "upper_bound_string";

    public static StatConfig getStatConfig (StatContainer statContainer) {
        return new StatConfig("graph.test_opensearch",
                Collections.singletonList("localhost"),
                9300,
                STAT_INDEX_NAME,
                STAT_TERM_TYPE_NAME,
                STAT_STRING_TYPE_NAME,
                STAT_NUMERIC_TYPE_NAME,
                STAT_GLOBAL_TYPE_NAME,
                STAT_COUNT_FIELD_NAME,
                STAT_CARDINALITY_FIELD_NAME,
                STAT_FIELD_TERM_NAME,
                STAT_FIELD_NUMERIC_DOUBLE_LOWER_NAME,
                STAT_FIELD_NUMERIC_DOUBLE_UPPER_NAME,
                STAT_FIELD_NUMERIC_LONG_LOWER_NAME,
                STAT_FIELD_NUMERIC_LONG_UPPER_NAME,
                STAT_FIELD_STRING_LOWER_NAME,
                STAT_FIELD_STRING_UPPER_NAME,
                statContainer);
    }
}
