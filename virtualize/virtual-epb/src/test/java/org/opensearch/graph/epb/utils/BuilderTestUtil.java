package org.opensearch.graph.epb.utils;

import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by moti on 3/2/2017.
 */
public class BuilderTestUtil {
    public static Pair<AsgQuery, AsgEBase> createSingleEntityQuery() {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        AsgEBase<EConcrete> ebaseAsgEBase = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(ebaseAsgEBase).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return new ImmutablePair<>(query, ebaseAsgEBase);
    }

    public static Pair<AsgQuery, AsgEBase<? extends EBase>> createTwoEntitiesPathQuery() {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.Builder.<EUntyped>get().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        AsgEBase<Rel> relAsg2 = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return new ImmutablePair<>(query, concreteAsg1);
    }

}
