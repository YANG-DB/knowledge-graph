package org.opensearch.graph.dispatcher.asg.builder;



import org.opensearch.graph.dispatcher.asg.BellowFactory;
import org.opensearch.graph.model.query.*;
import org.opensearch.graph.model.query.aggregation.Agg;
import org.opensearch.graph.model.query.aggregation.CountComp;
import org.opensearch.graph.model.query.entity.*;
import org.opensearch.graph.model.query.optional.OptionalComp;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.query.quant.HQuant;
import org.opensearch.graph.model.query.quant.Quant1;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */

public class BNextFactory implements BellowFactory {

    //region Constructor
    public BNextFactory() {
        this.map = new HashMap<>() ;
        this.map.put(HQuant.class, ebase -> ((HQuant)ebase).getB());
        this.map.put(Agg.class, ebase -> ((Agg)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Agg) ebase).getB()));
        this.map.put(Quant1.class, ebase -> ((Quant1)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Quant1) ebase).getB()));
        this.map.put(Rel.class, ebase -> ((Rel)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getB()));
        this.map.put(RelUntyped.class, ebase -> ((RelUntyped)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((RelUntyped) ebase).getB()));
        this.map.put(RelPattern.class, ebase -> ((Rel)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getB()));
        this.map.put(TypedEndPattern.class, ebase -> ((TypedEndPattern)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((TypedEndPattern) ebase).getB()));
        this.map.put(UnTypedEndPattern.class, ebase -> ((UnTypedEndPattern)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((UnTypedEndPattern) ebase).getB()));
        this.map.put(RelProp.class, ebase -> ((RelProp)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((RelProp) ebase).getB()));
        this.map.put(RelPropGroup.class, ebase -> (Collections.emptyList()));
        this.map.put(ETyped.class, ebase -> ((ETyped)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((ETyped) ebase).getB()));
        this.map.put(EUntyped.class, ebase -> (Collections.emptyList()));
        this.map.put(EAgg.class, ebase -> (Collections.emptyList()));
        this.map.put(EProp.class, ebase -> (Collections.emptyList()));
        this.map.put(CalculatedEProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EPropGroup.class, ebase -> (Collections.emptyList()));
        this.map.put(EConcrete.class, ebase -> (Collections.emptyList()));
        this.map.put(Start.class, ebase -> ((Start)ebase).getB() == 0 ? Collections.emptyList() : Collections.singletonList(((Start) ebase).getB()));
        this.map.put(OptionalComp.class, (ebase) -> Collections.emptyList());
        this.map.put(CountComp.class, (ebase) -> Collections.emptyList());
    }
    //endregion

    //region Public Methods
    public List<Integer> supplyBellow(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;

    //endregion
}
