package org.opensearch.graph.dispatcher.asg;







import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.asg.builder.BNextFactory;
import org.opensearch.graph.dispatcher.asg.builder.NextEbaseFactory;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.*;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;

public class QueryToAsgTransformer implements QueryTransformer<Query, AsgQuery> {
    //region Constructors
    @Inject
    public QueryToAsgTransformer() {
        this.nextFactory = new NextEbaseFactory();
        this.bellowFactory = new BNextFactory();
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(Query query) {
        if(query==null)
            throw new IllegalArgumentException("Query was null - probably serialization from input failed");

        Map<Integer, EBase> queryElements = new HashMap<>();
        Stream.ofAll(query.getElements()).forEach(eBase -> queryElements.put(eBase.geteNum(), eBase));

        //Working with the first element
        Start start = (Start) queryElements.get(0);

        //Building the root of the AsgQuery (i.e., start Ebase)
        AsgEBase asgEBaseStart = AsgEBase.Builder.get()
                .withEBase(start).build();

        queryAsgElements.put(asgEBaseStart.geteNum(),asgEBaseStart);
        buildSubGraphRec(asgEBaseStart, queryElements);

        AsgQuery.AsgQueryBuilder builder = AsgQuery.AsgQueryBuilder.anAsgQuery()
                .withName(query.getName())
                .withOnt(query.getOnt())
                .withOrigin(query)
                .withStart(asgEBaseStart)
                .withProjectedFields(query.getProjectedFields())
                .withElements(queryAsgElements.values());

        if(query instanceof ParameterizedQuery) {
            builder.withParams(((ParameterizedQuery) query).getParams());
        }
        return builder.build();
    }
    //endregion

    //region Private Methods
    private void buildSubGraphRec(AsgEBase asgEBaseCurrent, Map<Integer, EBase> queryElements) {
        EBase eBaseCurrent = asgEBaseCurrent.geteBase();

        Stream.ofAll(nextFactory.supplyNext(eBaseCurrent))
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


        Stream.ofAll(bellowFactory.supplyBellow(eBaseCurrent))
                .filter(b -> queryElements.get(b) != null)
                .forEach(
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
    private NextFactory nextFactory;
    private BellowFactory bellowFactory;
    private Map<Integer, AsgEBase<? extends EBase>> queryAsgElements = new HashMap<>();

    //endregion

}
