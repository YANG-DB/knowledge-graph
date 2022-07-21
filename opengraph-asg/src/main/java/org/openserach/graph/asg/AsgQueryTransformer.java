package org.openserach.graph.asg;




import com.google.inject.Inject;
import org.openserach.graph.asg.strategy.AsgStrategy;
import org.openserach.graph.asg.strategy.AsgStrategyRegistrar;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.QueryInfo;
import javaslang.collection.Stream;

import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class AsgQueryTransformer implements QueryTransformer<AsgQuery, AsgQuery> {
    //region Constructors
    @Inject
    public AsgQueryTransformer(AsgStrategyRegistrar asgStrategyRegistrar,
                               OntologyProvider ontologyProvider) {
        this.asgStrategies = asgStrategyRegistrar.register();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(AsgQuery query) {
        if(query==null)
            throw new IllegalArgumentException("Query was null - probably serialization from input failed");

        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            throw new RuntimeException("unknown ontology");
        }

        AsgStrategyContext asgStrategyContext =  new AsgStrategyContext(new Ontology.Accessor(ontology.get()));
        Stream.ofAll(this.asgStrategies)
                .forEach(strategy -> strategy.apply(query,asgStrategyContext));

        return query;
    }
    //endregion

    //region Fields
    private Iterable<AsgStrategy> asgStrategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
