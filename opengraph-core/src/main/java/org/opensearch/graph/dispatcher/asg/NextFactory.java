package org.opensearch.graph.dispatcher.asg;





import org.opensearch.graph.model.query.EBase;

import java.util.List;

public interface NextFactory {
    //region Public Methods
    List<Integer> supplyNext(EBase eBase);
}
