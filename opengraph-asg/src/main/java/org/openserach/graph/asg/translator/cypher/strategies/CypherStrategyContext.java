package org.openserach.graph.asg.translator.cypher.strategies;





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
