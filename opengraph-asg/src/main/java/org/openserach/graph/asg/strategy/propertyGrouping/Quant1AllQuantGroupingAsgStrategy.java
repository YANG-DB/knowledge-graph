package org.openserach.graph.asg.strategy.propertyGrouping;





import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.concurrent.atomic.AtomicBoolean;

public class Quant1AllQuantGroupingAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AtomicBoolean hasWorkToDo = new AtomicBoolean(true);
        while(hasWorkToDo.get()) {
            hasWorkToDo.set(false);

            AsgQueryUtil.elements(query, Quant1.class).forEach(quant -> {
                if (quant.geteBase().getqType().equals(QuantType.all)) {
                    AsgQueryUtil.<Quant1, Quant1>nextAdjacentDescendants(quant, Quant1.class).forEach(childQuant -> {
                        if (childQuant.geteBase().getqType().equals(QuantType.all)) {
                            hasWorkToDo.set(true);
                            AsgQueryUtil.replaceParents(quant,childQuant);
                            AsgQueryUtil.remove(query,childQuant);
                        }
                    });
                }
            });
        }
    }
    //endregion
}
