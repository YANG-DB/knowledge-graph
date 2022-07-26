package org.openserach.graph.asg.strategy.constraint;







import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.List;
import java.util.Map;

public class UnionETagDedupTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());
        map.values()
                .stream()
                .filter(v->v.size()>1)
                .forEach(this::dedupEtags);
    }

    private void dedupEtags(List<AsgEBase<EBase>> list) {
        list
                .stream()
                .skip(1)
                .map(AsgEBase::geteBase)
                .forEach(e-> ((Tagged)e).seteTag(String.format("%s:%d",((Tagged)e).geteTag(),e.geteNum())));
    }

    //endregion
}




