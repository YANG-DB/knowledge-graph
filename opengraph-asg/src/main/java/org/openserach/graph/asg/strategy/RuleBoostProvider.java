package org.openserach.graph.asg.strategy;




import org.opensearch.graph.model.query.properties.EProp;

public interface RuleBoostProvider {

    long getBoost(EProp eProp, int ruleIndex);

}
