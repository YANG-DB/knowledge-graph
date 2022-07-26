package org.openserach.graph.asg.translator.sparql;





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;
import org.opensearch.graph.model.transport.CreateQueryRequestMetadata;

import java.util.function.Function;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryLanguage.cypher;
import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryLanguage.sparql;

public class AsgSparQLTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery>, Function<QueryInfo<String>,Boolean> {
    public static final String transformerName = "AsgSparQLTransformer.@transformer";

    private SparqlTranslator translator;


    @Inject
    public AsgSparQLTransformer(SparqlTranslator translator) {
        this.translator = translator;
    }

    public AsgQuery transform(QueryInfo<String> query) {
        return translator.translate(query);
    }

    @Override
    public Boolean apply(QueryInfo<String> queryInfo) {
        return sparql.name().equalsIgnoreCase(queryInfo.getQueryType());
    }

}
