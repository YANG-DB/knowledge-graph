package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.ArrayList;
import java.util.List;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.max;

public interface SparqlUtils {

    static AsgEBase<? extends EBase> quant(AsgEBase<? extends EBase> byTag,
                                           AsgQuery query, SparqlStrategyContext context,
                                           QuantType parentQuantType) {
        //next find the quant associated with this element - if none found create one
        if (AsgQueryUtil.nextAdjacentDescendants(byTag, QuantBase.class).stream().noneMatch(g -> ((QuantBase) g.geteBase()).getqType().equals(parentQuantType))) {
            //quants will get enum according to the next formula = scopeElement.enum * 100
            final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(max(query) +1,  parentQuantType, new ArrayList<>(), 0));
            //is scope already has next - add them to the newly added quant
            if (context.getScope().hasNext()) {
                final List<AsgEBase<? extends EBase>> next = context.getScope().getNext();
                quantAsg.setNext(new ArrayList<>(next));
                context.getScope().setNext(new ArrayList<>());
            }
            context.getScope().addNext(quantAsg);
            context.scope(quantAsg);
        }
        return AsgQueryUtil.nextAdjacentDescendants(byTag, QuantBase.class).stream()
                .filter(g -> ((QuantBase) g.geteBase()).getqType().equals(parentQuantType))
                .findFirst().get();
    }

}
