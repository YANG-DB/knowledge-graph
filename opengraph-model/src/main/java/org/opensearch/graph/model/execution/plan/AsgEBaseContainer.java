package org.opensearch.graph.model.execution.plan;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.query.EBase;

/**
 * Created by Roman on 11/25/2017.
 */
public interface AsgEBaseContainer<T extends EBase> {
    AsgEBase<T> getAsgEbase();
}
