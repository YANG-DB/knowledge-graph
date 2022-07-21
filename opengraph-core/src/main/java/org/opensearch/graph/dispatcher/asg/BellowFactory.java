package org.opensearch.graph.dispatcher.asg;




import org.opensearch.graph.model.query.EBase;

import java.util.List;

/**
 * Created by lior.perry on 6/1/2017.
 */
public interface BellowFactory {
    //region Public Methods
    List<Integer> supplyBellow(EBase eBase);
}
