package org.openserach.graph.asg.translator;


import org.opensearch.graph.model.asgQuery.AsgQuery;

public interface AsgTranslator<S,T extends AsgQuery> {
    T translate(S source);
}
