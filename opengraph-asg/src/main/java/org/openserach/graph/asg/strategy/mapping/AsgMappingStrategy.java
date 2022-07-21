package org.openserach.graph.asg.strategy.mapping;



import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyMappingProvider;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.Value;
import org.opensearch.graph.model.ontology.mapping.MappingOntologies;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.resourceInfo.FuseError;
import javaslang.Tuple2;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.max;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.quant.QuantType.all;

public class AsgMappingStrategy implements AsgStrategy {
    public AsgMappingStrategy(OntologyProvider ontologyProvider, OntologyMappingProvider mappingProvider) {
        this.ontologyProvider = ontologyProvider;
        this.mappingProvider = mappingProvider;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //only operate on mapping related ontologies
        if (!mappingProvider.get(query.getOnt()).isPresent())
            return;

        MappingOntologies mappingOntologies = mappingProvider.get(query.getOnt()).get();
        Optional<Ontology> sourceOntology = ontologyProvider.get(mappingOntologies.getSourceOntology());
        Optional<Ontology> targetOntology = ontologyProvider.get(mappingOntologies.getTargetOntology());

        if (!sourceOntology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("No Ontology found ", "No Ontology found for " + mappingOntologies.getSourceOntology()));

        if (!targetOntology.isPresent())
            return;

        this.sourceAccessor = new Ontology.Accessor(sourceOntology.get());
        this.targetAccessor = new Ontology.Accessor(targetOntology.get());

        //for each entity replace source entity type with target entity type according to mapping by source.field to target.field
        mappingOntologies.getEntityTypes().forEach(e -> replaceEtype(e, query, context));
        //for each entity replace source entity type with target entity type according to mapping by source.field to target.field
        mappingOntologies.getRelationshipTypes().forEach(r -> replaceRelType(r, query, context));

        //replace ontology name with the newly mapped one
        query.setOnt(mappingOntologies.getTargetOntology());

    }

    private void replaceRelType(MappingOntologies.RelationshipType relationshipType, AsgQuery query, AsgStrategyContext context) {
        // for each mapping.source.relTypes[]
        //      - find all such rel in query
        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> (asgEBase.geteBase() instanceof Rel)
                && relationshipType.getSource().contains(((Rel) asgEBase.geteBase()).getrType()));

        /*     - replace each with mapping.target.relType according to source.field to target.field mapping instruction
         *          - switch (typeOf(target.field))
         *              case: string - copy the source
         *              case: enum - set enum.value(source)
         */
        Tuple2<Ontology.Accessor.NodeType, String> fieldType = targetAccessor.matchNameToType(relationshipType.getTargetField())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology field found for " + relationshipType.getTargetField())));

        switch (fieldType._1) {
            case ENTITY:
                //todo
                break;
            case ENUM:
                //todo
                break;
            case PROPERTY:
                Property property = targetAccessor.property$(fieldType._2);
                replaceByProperty(relationshipType, property, elements, query, context);
                break;
        }

    }

    /**
     * for each mapping.source.entityTypes[]
     * - find all such entities in query
     * - replace each with mapping.target.entityType according to source.field to target.field mapping instruction
     * - switch (typeOf(target.field))
     * case: string - copy the source
     * case: enum - set enum.value(source)
     *
     * @param entityType
     * @param query
     * @param context
     */
    private void replaceEtype(MappingOntologies.EntityType entityType, AsgQuery query, AsgStrategyContext context) {
        // for each mapping.source.entityTypes[]
        //      - find all such entities in query
        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query,
                asgEBase -> (asgEBase.geteBase() instanceof ETyped)
                && entityType.getSource().contains(((ETyped) asgEBase.geteBase()).geteType()));

        /*     - replace each with mapping.target.entityType according to source.field to target.field mapping instruction
         *          - switch (typeOf(target.field))
         *              case: string - copy the source
         *              case: enum - set enum.value(source)
         */
        Tuple2<Ontology.Accessor.NodeType, String> fieldType = targetAccessor.matchNameToType(entityType.getTargetField())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology field found for " + entityType.getTargetField())));

        switch (fieldType._1) {
            case ENTITY:
                //todo
                break;
            case ENUM:
                //todo
                break;
            case PROPERTY:
                Property property = targetAccessor.property$(fieldType._2);
                replaceByProperty(entityType, property, elements, query, context);
                break;
        }
    }

    /**
     * replace the property type according to the mapping schema
     *
     * @param relationshipType
     * @param property
     * @param elements
     * @param query
     * @param context
     */
    private void replaceByProperty(MappingOntologies.RelationshipType relationshipType , Property property, List<AsgEBase<EBase>> elements, AsgQuery query, AsgStrategyContext context) {
        elements.forEach(element -> replaceRel(element, property, relationshipType, query, context));

    }
    /**
     * replace the property type according to the mapping schema
     *
     * @param entityType
     * @param property
     * @param elements
     * @param query
     * @param context
     */
    private void replaceByProperty(MappingOntologies.EntityType entityType, Property property, List<AsgEBase<EBase>> elements, AsgQuery query, AsgStrategyContext context) {
        elements.forEach(element -> replaceEntity(element, property, entityType, query, context));

    }

    /**
     * search for descendent field of name $target.field if exist ?
     * - replace content using entityType.getSourceField()
     * otherwise
     * - add this new field as property to query
     *
     * @param element
     * @param property
     * @param entityType
     * @param query
     * @param context
     */
    private void replaceEntity(AsgEBase<EBase> element, Property property, MappingOntologies.EntityType entityType, AsgQuery query, AsgStrategyContext context) {
        if (AsgQueryUtil.nextAdjacentDescendant(element, EPropGroup.class, 2).isPresent()) {
            //add field to group
            AsgEBase<EBase> groupAsg = AsgQueryUtil.nextAdjacentDescendant(element, EPropGroup.class, 2).get();
            ((EPropGroup) groupAsg.geteBase()).addOrReplace(new EProp(groupAsg.geteBase().geteNum(), entityType.getTargetField(), constraintTransformed(((ETyped) element.geteBase()).geteType(), property)));
        } else {
            //create quant if needed and add group to quant - that group contains the needed type prop
            AsgEBase<? extends EBase> quant = AsgQueryUtil.createOrGetQuant(element, query, all);
            quant.addNext(new AsgEBase<>(new EPropGroup(max(query)+1,new EProp(max(query)+1, entityType.getTargetField(), constraintTransformed(((ETyped) element.geteBase()).geteType(), property)))));
        }
        //finally replace with the target designated entity Type
        ((ETyped) element.geteBase()).seteType(entityType.getTarget());
    }

    /**
     * search for descendent field of name $target.field if exist ?
     * - replace content using relationshipType.getSourceField()
     * otherwise
     * - add this new field as property to query
     *
     * @param element
     * @param property
     * @param relationshipType
     * @param query
     * @param context
     */
    private void replaceRel(AsgEBase<EBase> element, Property property, MappingOntologies.RelationshipType relationshipType, AsgQuery query, AsgStrategyContext context) {
        if (AsgQueryUtil.bAdjacentDescendant(element, RelPropGroup.class).isPresent()) {
            //add field to group
            AsgEBase<EBase> groupAsg = AsgQueryUtil.bAdjacentDescendant(element, RelPropGroup.class).get();
            ((RelPropGroup) groupAsg.geteBase()).addOrReplace(new RelProp(groupAsg.geteBase().geteNum(), relationshipType.getTargetField(), constraintTransformed(((Rel) element.geteBase()).getrType(), property)));
        } else {
            //create quant if needed and add group to quant - that group contains the needed type prop
            element.getB().add(new AsgEBase<>(new RelPropGroup(max(query)+1,new RelProp(max(query)+1, relationshipType.getTargetField(), constraintTransformed(((Rel) element.geteBase()).getrType(), property)))));
        }
        //finally replace with the target designated rel Type
        ((Rel) element.geteBase()).setrType(relationshipType.getTarget());
    }

    private Constraint constraintTransformed(String type, Property property) {
        if (targetAccessor.enumeratedType(property.getType()).isPresent()) {
            //if target type is enum - use the source value for enum.valueOf for target constraint
            Value value = targetAccessor.enumeratedType$(property.getType()).valueOf(type)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology Enumerated value found", "No target Ontology Enumerated value found for " + type)));
            //set enum index value as constraint
            return new Constraint(eq, value.getVal());
        } else {
            //if target type is non-enum (string ?) - use the source value for direct target value constraint
            return new Constraint(eq, type);
        }
    }

    //region Fields
    private Ontology.Accessor sourceAccessor;
    private Ontology.Accessor targetAccessor;
    private OntologyProvider ontologyProvider;
    private OntologyMappingProvider mappingProvider;


}
