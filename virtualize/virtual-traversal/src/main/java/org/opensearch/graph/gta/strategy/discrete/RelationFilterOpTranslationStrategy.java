package org.opensearch.graph.gta.strategy.discrete;

/*-
 * #%L
 * virtual-traversal
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.utils.ConversionUtil;
import org.opensearch.graph.gta.strategy.utils.TraversalUtil;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.FunctionRelProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.SchematicRelProp;
import org.opensearch.graph.model.query.properties.constraint.WhereByConstraint;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.opensearch.graph.unipop.promise.Constraint;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.GlobalConstants.HasKeys.CONSTRAINT;

public class RelationFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationFilterOpTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(RelationFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        RelationFilterOp relationFilterOp = (RelationFilterOp) planOp;
        Optional<RelationOp> relationOp = PlanUtil.adjacentPrev(plan.getPlan(), relationFilterOp);
        if (!relationOp.isPresent()) {
            return traversal;
        }

        TraversalUtil.remove(traversal, TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class));

        traversal = appendRelationAndPropertyGroup(
                traversal,
                relationOp.get().getAsgEbase().geteBase(),
                relationFilterOp.getAsgEbase().geteBase(),
                context.getOnt());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendRelationAndPropertyGroup(
            GraphTraversal traversal,
            Rel rel,
            RelPropGroup relPropGroup,
            Ontology.Accessor ont) {

        String relationTypeName = ont.$relation$(rel.getrType()).getName();

        List<Traversal> relPropGroupTraversals = Collections.emptyList();
        if (!relPropGroup.getProps().isEmpty() || !relPropGroup.getGroups().isEmpty()) {
            relPropGroupTraversals = Collections.singletonList(convertRelPropGroupToTraversal(relPropGroup, ont));
        }


        List<Traversal> traversals = Stream.<Traversal>of(__.start().has(T.label, P.eq(relationTypeName)))
                .appendAll(relPropGroupTraversals).toJavaList();

        return traversals.size() == 1 ?
                traversal.has(CONSTRAINT, Constraint.by(traversals.get(0))) :
                traversal.has(CONSTRAINT, Constraint.by(__.start().and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }
    //endregion

    //region Private Methods
    private Traversal convertRelPropGroupToTraversal(RelPropGroup relPropGroup, Ontology.Accessor ont) {
        List<Traversal> childGroupTraversals = Stream.ofAll(relPropGroup.getGroups())
                .map(childGroup -> convertRelPropGroupToTraversal(childGroup, ont))
                .toJavaList();

        List<Traversal> epropTraversals = Stream.ofAll(relPropGroup.getProps())
                .filter(relProp -> relProp.getCon() != null)
                .filter(relProp -> !(relProp.getCon() instanceof WhereByConstraint))
                .map(relProp -> convertRelPropToTraversal(relProp, ont))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        Traversal[] traversals = Stream.ofAll(epropTraversals).appendAll(childGroupTraversals).toJavaArray(Traversal.class);

        switch (relPropGroup.getQuantType()) {
            case all:
                if (traversals.length == 1) {
                    return traversals[0];
                }

                return __.start().and(traversals);
            case some:
                return __.start().or(traversals);

            default:
                return __.start().and(traversals);
        }
    }

    private Optional<Traversal> convertRelPropToTraversal(RelProp relProp, Ontology.Accessor ont) {
        if (!relProp.isAggregation()) {
            //row based constraint
            Optional<Property> property = ont.$property(relProp.getpType());
            if (property.isPresent()) {
                if (relProp.getClass().equals(RelProp.class)) {
                    return Optional.of(__.start().has(property.get().getName(), ConversionUtil.convertConstraint(relProp.getCon())));
                } else if (SchematicRelProp.class.isAssignableFrom(relProp.getClass())) {
                    return Optional.of(__.start().has(((SchematicRelProp) relProp).getSchematicName(),
                            ConversionUtil.convertConstraint(relProp.getCon())));
                }
            }
        } else {
            //aggregation based constraint -
            String foreignKeyName = GlobalConstants.EdgeSchema.SOURCE_ID;//todo take this from the schema
            if (relProp.getClass().equals(FunctionRelProp.class)) {
                return Optional.of(__.start().has(foreignKeyName, ConversionUtil.convertConstraint(relProp.getCon())));
            } else if (SchematicRelProp.class.isAssignableFrom(relProp.getClass())) {
                return Optional.of(__.start().has(((SchematicRelProp) relProp).getSchematicName(),
                        ConversionUtil.convertConstraint(relProp.getCon())));
            }
        }
        return Optional.empty();
    }
    //endregion
}
