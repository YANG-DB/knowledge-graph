package org.opensearch.graph.dispatcher.asg;




import com.google.common.base.Supplier;
import org.opensearch.graph.dispatcher.asg.builder.BNextFactory;
import org.opensearch.graph.dispatcher.asg.builder.NextEbaseFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Start;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benishue on 27-Feb-17.
 */
public class AsgQuerySupplier implements Supplier<AsgQuery> {

    //region Constructor
    public AsgQuerySupplier(Query query) {
        this(query,new NextEbaseFactory(), new BNextFactory());
    }

    public AsgQuerySupplier(Query query, NextFactory nextFactory, BellowFactory bellowFactory) {
        this.query = query;
        this.factory = nextFactory;
        this.bellowFactory = bellowFactory;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public AsgQuery get() {
        Map<Integer, EBase> queryElements = new HashMap<>();
        Stream.ofAll(query.getElements()).forEach(eBase -> queryElements.put(eBase.geteNum(), eBase));

        //Working with the first element
        Start start = (Start) queryElements.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        AsgEBase asgEBaseStart = AsgEBase.Builder.get()
                .withEBase(start).build();

        queryAsgElements.put(asgEBaseStart.geteNum(),asgEBaseStart);
        buildSubGraphRec(asgEBaseStart, queryElements);

        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOrigin(query)
                .withOnt(query.getOnt())
                .withStart(asgEBaseStart)
                .withElements(queryAsgElements.values())
                .build();
        return asgQuery;
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(AsgEBase asgEBaseCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = asgEBaseCurrent.geteBase();

        Stream.ofAll(factory.supplyNext(eBaseCurrent))
                .filter(b -> queryElements.get(b) != null)
                .forEach(eNum -> {
                    EBase eBaseNext = queryElements.get(eNum);
                    AsgEBase asgEBaseNext = AsgEBase.Builder.get()
                            .withEBase(eBaseNext)
                            .build();
                    queryAsgElements.put(eNum,asgEBaseNext);
                    asgEBaseCurrent.addNextChild(asgEBaseNext);

                    buildSubGraphRec(asgEBaseNext, queryElements);
                });


        Stream.ofAll(bellowFactory.supplyBellow(eBaseCurrent)).forEach(
                eNum -> {
                    EBase eBaseB = queryElements.get(eNum);
                    AsgEBase asgEBaseB = AsgEBase.Builder.get()
                            .withEBase(eBaseB)
                            .build();

                    queryAsgElements.put(eNum,asgEBaseB);
                    asgEBaseCurrent.addBChild(asgEBaseB);
                    buildSubGraphRec(asgEBaseB, queryElements);
                }
        );
    }

    //endregion

    //region Fields
    private Query query;
    private NextFactory factory;
    private BellowFactory bellowFactory;
    private Map<Integer, AsgEBase<? extends EBase>> queryAsgElements = new HashMap<>();

    //endregion
}
