package org.opensearch.graph.dispatcher.asg.builder;






import org.opensearch.graph.dispatcher.asg.NextFactory;
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

public class NextEbaseFactory implements NextFactory {

    //region Constructor
    public NextEbaseFactory() {
        this.map = new HashMap<>() ;
        this.map.put(Agg.class, (ebase) -> ((Agg)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Agg) ebase).getNext()));
        this.map.put(HQuant.class, (ebase) -> (Collections.emptyList()));
        this.map.put(RelProp.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(RelPropGroup.class, (ebase) ->  (Collections.emptyList()));
        this.map.put(ETyped.class, (ebase) -> ((ETyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((ETyped) ebase).getNext()));
        this.map.put(EUntyped.class, (ebase) -> ((EUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EUntyped) ebase).getNext()));
        this.map.put(EAgg.class, (ebase) -> ((EAgg)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EAgg) ebase).getNext()));
        this.map.put(EConcrete.class, (ebase) -> ((EConcrete)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((EConcrete) ebase).getNext()));
        this.map.put(EProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(CalculatedEProp.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EPropGroup.class, (ebase) -> (Collections.emptyList()));
        this.map.put(Quant1.class, (ebase) -> ((Quant1) ebase).getNext());
        this.map.put(Rel.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getNext()));
        this.map.put(RelUntyped.class, (ebase) -> ((RelUntyped)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((RelUntyped) ebase).getNext()));
        this.map.put(RelPattern.class, (ebase) -> ((Rel)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Rel) ebase).getNext()));
        this.map.put(TypedEndPattern.class, (ebase) -> ((TypedEndPattern)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((TypedEndPattern) ebase).getNext()));
        this.map.put(UnTypedEndPattern.class, (ebase) -> ((UnTypedEndPattern)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((UnTypedEndPattern) ebase).getNext()));
        this.map.put(Start.class, (ebase) -> ((Start)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((Start) ebase).getNext()));
        this.map.put(OptionalComp.class, (ebase) -> ((OptionalComp)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((OptionalComp)ebase).getNext()));
        this.map.put(CountComp.class, (ebase) -> ((CountComp)ebase).getNext() == 0 ? Collections.emptyList() : Collections.singletonList(((CountComp)ebase).getNext()));
    }
    //endregion

    //region Public Methods
    @Override
    public List<Integer> supplyNext(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;
    //endregion
}
