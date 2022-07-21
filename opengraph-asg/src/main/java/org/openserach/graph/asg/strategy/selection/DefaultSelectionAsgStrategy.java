package org.openserach.graph.asg.strategy.selection;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Roman on 13/06/2017.
 *
 * Go over all EPropGroups - if no projection was requested - go over all existing entity's props and add them as default
 * - similar to "select * from entity type"
 */
public class DefaultSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
        this.nonSelectablePTypes = Stream.of(OntologyFinalizer.ID_FIELD_PTYPE, OntologyFinalizer.TYPE_FIELD_PTYPE)
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
                    if (Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                            .filter(eProp -> !(eProp instanceof CalculatedEProp))
                            .filter(eProp -> eProp.getProj() != null)
                            .isEmpty()) {

                        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, ETyped.class);
                        List<EProp> projectionProps = Collections.emptyList();

                         if (eTypedAsgEBase.isPresent()) {
                            projectionProps =
                                    Stream.ofAll(ont.$entity$(eTypedAsgEBase.get().geteBase().geteType()).getProperties())
                                    .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                    .map(pType -> new EProp(0, pType, new IdentityProjection()))
                                    .toJavaList();
                        } else {
                            Optional<AsgEBase<EUntyped>> eUntypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EUntyped.class);
                            if (eUntypedAsgEBase.isPresent()) {
                                projectionProps =
                                        Stream.ofAll(ont.entities())
                                        .flatMap(EntityType::getProperties)
                                        .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                        .map(pType -> new EProp(0, pType, new IdentityProjection()))
                                        .toJavaList();
                            }
                        }

                        if (ePropGroupAsgEBase.geteBase().getQuantType().equals(QuantType.all)) {
                            ePropGroupAsgEBase.geteBase().getProps().addAll(projectionProps);
                        } else if (ePropGroupAsgEBase.geteBase().getQuantType().equals(QuantType.some)) {
                             EPropGroup clone = new EPropGroup(
                                     0,
                                     QuantType.some,
                                     ePropGroupAsgEBase.geteBase().getProps(),
                                     ePropGroupAsgEBase.geteBase().getGroups());

                             ePropGroupAsgEBase.geteBase().getProps().clear();
                             ePropGroupAsgEBase.geteBase().getGroups().clear();
                             ePropGroupAsgEBase.geteBase().setQuantType(QuantType.all);
                             ePropGroupAsgEBase.geteBase().getGroups().add(clone);
                             ePropGroupAsgEBase.geteBase().getProps().addAll(projectionProps);
                        }
                    }
                }
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private Set<String> nonSelectablePTypes;
    //endregion
}
