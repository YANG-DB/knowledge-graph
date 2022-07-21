package org.openserach.graph.asg.strategy.type;





import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.ontology.EPair;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.model.GlobalConstants._ALL;

public class UntypedInferTypeLeftSideRelationAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, EUntyped.class))
                .forEach(sideA -> {
                    //replace implicit types wildcard call with explicit types
                    if(sideA.geteBase().getvTypes().contains(_ALL)) {
                        //replace the "_all" statement with each existing type
                        sideA.geteBase().setvTypes(StreamSupport.stream(context.getOntologyAccessor().eTypes().spliterator(),true).collect(Collectors.toSet()));
                    }

                    Optional<AsgEBase<Rel>> relation = AsgQueryUtil.nextAdjacentDescendant(sideA, Rel.class);
                    if(relation.isPresent()) {
                        AsgEBase<Rel> rel = relation.get();
                        Optional<RelationshipType> relationshipType = context.getOntologyAccessor().$relation(rel.geteBase().getrType());
                        ArrayList<String> sideAvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeA).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());

                        //try populating side B of the rel is it is an Untyped
                        Optional<AsgEBase<EUntyped>> sideB = AsgQueryUtil.nextAdjacentDescendant(rel, EUntyped.class);
                        if(sideB.isPresent()) {
                            ArrayList<String> sideBvTypes = new ArrayList<>(relationshipType.get().getePairs().stream().map(EPair::geteTypeB).collect(Collectors.groupingBy(v -> v, Collectors.toSet())).keySet());
                            //populate possible types only if no types present on entity
                            if(sideB.get().geteBase().getvTypes().isEmpty()) {
                                sideB.get().geteBase().getvTypes().addAll(sideBvTypes);
                            }
                        }
                        //populate possible types only if no types present on entity
                        if(sideA.geteBase().getvTypes().isEmpty()) {
                            sideA.geteBase().getvTypes().addAll(sideAvTypes);
                        }
                    }
                });

    }
    //endregion
}
