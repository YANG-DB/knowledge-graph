package org.openserach.graph.asg.strategy.selection;



import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.common.Strings;

import static org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor.describe;

public class DefaultETagAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultETagAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());
        AsgQueryUtil.elements(query, element->(element.geteBase() instanceof Tagged)).forEach(e-> {
            if(Strings.isEmpty(((Tagged)e.geteBase()).geteTag())){
                //if no tag exists - create one based on the entity description
                ((Tagged)e.geteBase()).seteTag(describe(e.geteBase()));
            }
        });
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
