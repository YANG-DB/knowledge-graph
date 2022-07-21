package org.openserach.graph.asg.translator.cypher;



import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;

import java.util.function.Function;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryLanguage.cypher;

/**
 * Created by liorp on 12/15/2017.
 */
public class AsgCypherTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery>, Function<QueryInfo<String>,Boolean> {
    public static final String transformerName = "AsgCypherTransformer.@transformer";
    //region Constructors
    @Inject
    public AsgCypherTransformer(CypherTranslator cypherTranslator) {
        this.cypherTranslator = cypherTranslator;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(QueryInfo<String> query) {
        return cypherTranslator.translate(query);
    }
    //endregion

    //region Fields
    private CypherTranslator cypherTranslator;

    @Override
    public Boolean apply(QueryInfo<String> queryInfo) {
        return cypher.name().equalsIgnoreCase(queryInfo.getQueryType());
    }
    //endregion
}
