package org.opensearch.graph.asg.translator.cypher;

/*-
 * #%L
 * opengraph-asg
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */






import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.QueryInfo;

import java.util.function.Function;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryLanguage.cypher;

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
