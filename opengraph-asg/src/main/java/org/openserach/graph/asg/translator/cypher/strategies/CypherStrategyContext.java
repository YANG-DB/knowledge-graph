package org.openserach.graph.asg.translator.cypher.strategies;

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

import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.query.EBase;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.ast.Where;

import java.util.Optional;

public class CypherStrategyContext {

    public CypherStrategyContext(Statement statement, AsgEBase<? extends EBase> scope) {
        this.statement = statement;
        this.scope = scope;
        this.where = Optional.empty();
    }

    public AsgEBase<? extends EBase> getScope() {
        return scope;
    }

    public Statement getStatement() {
        return statement;
    }

    public CypherStrategyContext where(Where where) {
        this.where = Optional.of(CypherUtils.reWrite(where.expression()));
        return this;
    }

    public CypherStrategyContext scope(AsgEBase<? extends EBase> scope) {
        this.scope = scope;
        return this;
    }

    public Optional<com.bpodgursky.jbool_expressions.Expression> getWhere() {
        return where;
    }

    //region Fields
    private Statement statement;
    private Optional<com.bpodgursky.jbool_expressions.Expression> where ;

    private AsgEBase<? extends EBase> scope;

    //endregion
}
