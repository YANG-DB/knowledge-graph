package org.opensearch.graph.dispatcher.asg;




import org.opensearch.graph.model.query.EBase;

import java.util.List;

public interface BellowFactory {
    //region Public Methods
    List<Integer> supplyBellow(EBase eBase);
}
