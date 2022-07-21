package org.opensearch.graph.asg.strategy.schema;


import org.opensearch.graph.asg.strategy.schema.utils.LikeUtil;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.BasePropGroup;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.SchematicEProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.openserach.graph.asg.strategy.AsgElementStrategy;
import org.openserach.graph.asg.strategy.AsgStrategy;

import java.util.*;

import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.ngrams;

/**
 * Created by roman.margolis on 07/03/2018.
 */
public class LikeAnyConstraintTransformationAsgStrategy implements AsgStrategy, AsgElementStrategy<EPropGroup> {
    //region Constructors
    public LikeAnyConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> apply(query, ePropGroupAsgEBase, context));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;

    @Override
    public void apply(AsgQuery query, AsgEBase<EPropGroup> ePropGroupAsgEBase, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        Ontology.Accessor ont = new Ontology.Accessor(ontology.get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());

        //currently supporting only ETyped or EConcrete
        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EEntityBase.class);
        if (!eTypedAsgEBase.isPresent()) {
            return;
        }

        for (EProp eProp : new ArrayList<>(ePropGroupAsgEBase.geteBase().getProps())) {
            if (!eProp.isConstraint() || !eProp.getCon().getOp().equals(ConstraintOp.likeAny)) {
                continue;
            }

            Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.get().geteBase().geteType());
            if (Stream.ofAll(vertexSchemas).isEmpty()) {
                continue;
            }

            // currently supports a single vertex schema
            GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

            Optional<Property> property = ont.$property(eProp.getpType());
            if (!property.isPresent()) {
                continue;
            }

            Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get().getName());
            if (!propertySchema.isPresent()) {
                continue;
            }

            EPropGroup wildcardRuleGroup = new EPropGroup(
                    eProp.geteNum(),
                    QuantType.some,
                    Collections.emptyList(),
                    Stream.ofAll(CollectionUtil.listFromObjectValue(eProp.getCon().getExpr()))
                            .map(value -> new EPropGroup(
                                    eProp.geteNum(),
                                    LikeUtil.applyWildcardRules(
                                            EProp.of(eProp.geteNum(), eProp.getpType(), Constraint.of(ConstraintOp.like, value)),
                                            propertySchema.get()))));

            List<String> ngramWords =
                    Stream.ofAll(wildcardRuleGroup.getGroups())
                            .flatMap(BasePropGroup::getProps)
                            .flatMap(prop -> Stream.ofAll(LikeUtil.getWildcardNgrams(propertySchema.get(), prop.getCon().getExpr().toString())))
                            .distinct()
                            .toJavaList();

            Optional<EProp> ngramsOptimizationEprop =  propertySchema.get().getIndexingSchema(ngrams).isPresent() && !ngramWords.isEmpty()?
                    Optional.of(new SchematicEProp(
                            0,
                            eProp.getpType(),
                            propertySchema.get().getIndexingSchema(ngrams).get().getName(),
                            Constraint.of(ConstraintOp.inSet, ngramWords))) : Optional.empty();

            List<EPropGroup> simpleWildcardGroups = propertySchema.get().getIndexingSchema(ngrams).isPresent() ?
                    Stream.ofAll(wildcardRuleGroup.getGroups())
                            .filter(group -> group.getProps().size() == 1)
                            .filter(group -> group.getProps().get(0).getCon().getOp().equals(ConstraintOp.eq))
                            .filter(group -> group.getProps().get(0) instanceof SchematicEProp)
                            .filter(group -> ((SchematicEProp) group.getProps().get(0)).getSchematicName()
                                    .equals(propertySchema.get().getIndexingSchema(ngrams).get().getName()))
                            .toJavaList() : Collections.emptyList();

            if (!simpleWildcardGroups.isEmpty()) {
                wildcardRuleGroup.getProps().add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        propertySchema.get().getIndexingSchema(ngrams).get().getName(),
                        Constraint.of(
                                ConstraintOp.inSet,
                                Stream.ofAll(simpleWildcardGroups).map(group -> group.getProps().get(0).getCon().getExpr().toString()).toJavaList())));
            }
            simpleWildcardGroups.forEach(simpleWildcardGroup -> wildcardRuleGroup.getGroups().remove(simpleWildcardGroup));

            ePropGroupAsgEBase.geteBase().getProps().remove(eProp);

            ngramsOptimizationEprop.ifPresent(ngramsEprop -> ePropGroupAsgEBase.geteBase().getProps().add(ngramsEprop));

            if (!wildcardRuleGroup.getGroups().isEmpty() || !wildcardRuleGroup.getProps().isEmpty()) {
                ePropGroupAsgEBase.geteBase().getGroups().add(wildcardRuleGroup);
            }
        }
    }
    //endregion
}
