package org.opensearch.graph.executor.cursor.discrete;


import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.BaseCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.projection.CalculatedFieldProjection;
import org.opensearch.graph.model.results.*;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

import static org.opensearch.graph.executor.cursor.discrete.CalculatedFieldsUtil.findCalculaedFields;

public class GraphTraversalCursor extends BaseCursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new GraphTraversalCursor(new PathsTraversalCursor((TraversalCursorContext) context));
        }
        //endregion
    }
    //endregion

    //region Constructors
    public GraphTraversalCursor(Cursor<TraversalCursorContext> cursor) {
        super(cursor.getContext());
        this.cursor = cursor;

        this.fullGraph = new AssignmentsQueryResult<>();
        this.fullGraph.setAssignments(new ArrayList<>());
        this.fullGraph.getAssignments().add(new Assignment());
        this.fullGraph.getAssignments().get(0).setEntities(new ArrayList<>());
        this.fullGraph.getAssignments().get(0).setRelationships(new ArrayList<>());

        this.entityIds = new HashSet<>();
        this.entityTags = new HashSet<>();
        this.relationshipIds = new HashSet<>();
        this.relationshipTags = new HashSet<>();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        final Query pattern = getContext().getQueryResource().getQuery();
        this.fullGraph.setPattern(pattern);

        AssignmentsQueryResult newResult = (AssignmentsQueryResult) this.cursor.getNextResults(numResults);
        consolidateFullGraph(newResult);

        return this.fullGraph;
    }
    //endregion

    //region Protected Methods
    private void consolidateFullGraph(AssignmentsQueryResult<Entity, Relationship> result) {
        AsgQuery pattern = this.cursor.getContext().getQueryResource().getAsgQuery();

        //get unique entity tags
        Set<String> eTags = Stream.ofAll(result.getAssignments())
                .flatMap(Assignment::getEntities)
                .filter(entity -> !this.entityTags.contains(entity.geteTag()))
                .flatMap(e -> e.geteTag())
                .distinct()
                .toJavaSet();

        //add calculated fields of existing eTags
        Map<String, List<CalculatedEProp>> calculatedFieldsMap = Stream.ofAll(eTags)
                .toJavaMap(p -> new Tuple2<>(p, findCalculaedFields(pattern, p)));

        Map<String, Stream<Entity>> newEntityStreams =
                Stream.ofAll(result.getAssignments())
                        .flatMap(Assignment::getEntities)
                        .filter(entity -> !this.entityIds.contains(entity.geteID()))
                        .groupBy(Entity::geteID).toJavaMap();


        Map<String, Entity> newEntities = Stream.ofAll(newEntityStreams.values())
                .map(entityStream -> {
                    Entity.Builder entityBuilder = Entity.Builder.instance();
                    Stream.ofAll(entityStream).forEach(entityBuilder::withEntity);
                    return entityBuilder.build();
                })
                .toJavaMap(entity -> new Tuple2<>(entity.geteID(), entity));

        Map<String, Relationship> newRelationships =
                Stream.ofAll(result.getAssignments())
                        .flatMap(Assignment::getRelationships)
                        .filter(relationship -> !this.relationshipIds.contains(relationship.getrID()))
                        .distinctBy(Relationship::getrID)
                        .toJavaMap(relationship -> new Tuple2<>(relationship.getrID(), relationship));

        //count relations for specific pair of tags
        addCalculatedFields(calculatedFieldsMap, newEntities, newRelationships);

        this.fullGraph.getAssignments().get(0).getEntities().addAll(newEntities.values());
        this.fullGraph.getAssignments().get(0).getRelationships().addAll(newRelationships.values());

        this.fullGraph.getAssignments().get(0).setEntities(
                Stream.ofAll(this.fullGraph.getAssignments().get(0).getEntities())
                        .sortBy(Entity::geteType)
                        .toJavaList());

        this.entityIds.addAll(newEntities.keySet());
        this.relationshipIds.addAll(newRelationships.keySet());
    }

    private void addCalculatedFields(Map<String, List<CalculatedEProp>> calculatedFieldsMap, Map<String, Entity> newEntities, Map<String, Relationship> newRelationships) {
        //for each eTag name -> go over all its calculated fields definitions and...
        for (String entry : calculatedFieldsMap.keySet()) {
            //extract for each entityId a calculated number related to the specific calculated field
            calculatedFieldsMap.get(entry).forEach(field -> {
                Map<String, Integer> calculatedFieldsById = calculateFieldAgg(entry, field, newRelationships);
                calculatedFieldsById.entrySet().stream()
                        .filter(val -> newEntities.containsKey(val.getKey()))
                        //for each uniqueId in the relations -> add the calculated field with the calculated value
                        .forEach(val -> newEntities.get(val.getKey())
                                .setProperty(new Property(field.getProj().getExpression() +"["+ field.getpType()+"]", val.getValue())));
            });
        }
    }

    private Map<String, Integer> calculateFieldAgg(String eTag, CalculatedEProp prop, Map<String, Relationship> newRelationships) {
        return Stream.ofAll(newRelationships.values())
                .filter(v -> v.geteTag1().equals(eTag))
                .filter(v -> v.geteTag2().equals(prop.getpType()))
                .groupBy(Relationship::geteID1)
                .toJavaMap(p -> new Tuple2<>(p._1, agg(p._2.toJavaList(), prop.getProj())));
    }

    private int agg(List<Relationship> relationships, CalculatedFieldProjection con) {
        switch (con.getExpression()) {
            case count:
                return relationships.size();
            case max:
                //todo what ?!?!?
                return relationships.size();
            case min:
                //todo what ?!?!?
                return relationships.size();
            case avg:
                //todo what ?!?!?
                return relationships.size();
            case distinct:
                //todo what ?!?!?
                return relationships.size();
            case sum:
                //todo what ?!?!?
                return relationships.size();
            default:
                return relationships.size();
        }
    }

//endregion

    //region Fields
    private Cursor<TraversalCursorContext> cursor;
    private AssignmentsQueryResult<Entity, Relationship> fullGraph;

    private Set<String> entityIds;
    private Set<String> entityTags;
    private Set<String> relationshipIds;
    private Set<String> relationshipTags;
    //endregion
}
