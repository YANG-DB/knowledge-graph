package org.opensearch.graph.executor.cursor.discrete;


import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.executor.cursor.BaseCursor;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.executor.utils.ConversionUtil;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;

public class PathsTraversalCursor extends BaseCursor {

    public static final String RAW = "raw";

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new PathsTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context) {
        super(context);
        this.ont = new Ontology.Accessor(context.getOntology());

        this.includeEntities = context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.all) ||
                context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.entities);
        this.includeRelationships = context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.all) ||
                context.getCursorRequest().getInclude().equals(CreateCursorRequest.Include.relationships);

        Plan flatPlan = PlanUtil.flat(context.getQueryResource().getExecutionPlan().getPlan());
        if (this.includeEntities) {
            this.eEntityBases = Stream.ofAll(flatPlan.getOps())
                    .filter(planOp -> planOp instanceof EntityOp)
                    .map(planOp -> (EntityOp) planOp)
                    .toJavaMap(planOp -> new Tuple2<>(planOp.getAsgEbase().geteBase().geteTag(), planOp.getAsgEbase().geteBase()));
        }

        if (this.includeRelationships) {
            this.eRels = Stream.ofAll(flatPlan.getOps())
                    .filter(planOp -> planOp instanceof RelationOp)
                    .toJavaMap(planOp -> {
                        RelationOp relationOp = (RelationOp) planOp;
                        Optional<EntityOp> prevEntityOp =
                                PlanUtil.prev(flatPlan, planOp, EntityOp.class);
                        Optional<EntityOp> nextEntityOp =
                                PlanUtil.next(flatPlan, planOp, EntityOp.class);

                        String relationLabel = prevEntityOp.get().getAsgEbase().geteBase().geteTag() +
                                ConversionUtil.convertDirectionGraphic(relationOp.getAsgEbase().geteBase().getDir()) +
                                nextEntityOp.get().getAsgEbase().geteBase().geteTag();

                        return new Tuple2<>(relationLabel,
                                new Tuple3<>(prevEntityOp.get().getAsgEbase().geteBase(),
                                        relationOp.getAsgEbase().geteBase(),
                                        nextEntityOp.get().getAsgEbase().geteBase()));
                    });
        }

        this.typeProperty = this.ont.property$("type");
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        return toQuery(numResults);
    }
    //endregion

    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = context.getQueryResource().getQuery();
        builder.withPattern(pattern)
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime());

        //build assignments
        List<Path> paths = context.next(numResults);
        paths.forEach(path -> builder.withAssignment(toAssignment(path)));
        return builder.build();
    }


    protected Assignment toAssignment(Path path) {
//     Todo should be:  Assignment.Builder<Entity,Relationship> builder = Assignment.Builder.instance();
        Assignment.Builder builder = Assignment.Builder.instance();

        List<Object> pathObjects = path.objects();
        List<Set<String>> pathlabels = path.labels();
        for (int objectIndex = 0; objectIndex < pathObjects.size(); objectIndex++) {
            Object pathObject = pathObjects.get(objectIndex);
            String pathLabel = pathlabels.get(objectIndex).iterator().next();

            if (Vertex.class.isAssignableFrom(pathObject.getClass()) && this.includeEntities) {
                builder.withEntity(toEntity((Vertex) pathObject, this.eEntityBases.get(pathLabel)));
            } else if (Edge.class.isAssignableFrom(pathObject.getClass()) && this.includeRelationships) {
                Tuple3<EEntityBase, Rel, EEntityBase> relTuple = this.eRels.get(pathLabel);
                builder.withRelationship(toRelationship(
                        (Edge) pathObject,
                        relTuple._1(),
                        relTuple._2(),
                        relTuple._3()));
            } else {
                throw new UnsupportedOperationException("unexpected object in path");
            }
        }

        return builder.build();
    }

    protected org.opensearch.graph.model.logical.Vertex<Entity> toEntity(Vertex vertex, EEntityBase element) {
        String eType = vertex.label();
        List<Property> properties = Stream.ofAll(vertex::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        Entity.Builder builder = Entity.Builder.instance();
        builder.withEID(vertex.id().toString());
        builder.withEType(eType);
        builder.withETag(new HashSet<>(Collections.singletonList(element.geteTag())));
        builder.withProperties(properties);
        return builder.build();
    }

    protected Relationship toRelationship(Edge edge, EEntityBase prevEntity, Rel rel, EEntityBase nextEntity) {
        Relationship.Builder builder = Relationship.Builder.instance();
        builder.withRID(edge.id().toString());
        builder.withRType(rel.getrType());
        builder.withRTag(rel.geteTag());
        builder.withEID1(edge.outVertex().id().toString());
        builder.withEID2(edge.inVertex().id().toString());
        //add properties
        List<Property> properties = Stream.ofAll(edge::properties)
                .map(this::toProperty)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toJavaList();

        builder.withProperties(properties);

        switch (rel.getDir()) {
            case R:
                builder.withETag1(prevEntity.geteTag());
                builder.withETag2(nextEntity.geteTag());
                break;

            case L:
                builder.withETag1(nextEntity.geteTag());
                builder.withETag2(prevEntity.geteTag());
        }

        return builder.build();
    }

    protected Optional<Property> toProperty(org.apache.tinkerpop.gremlin.structure.Property vertexProperty) {
        return Stream.of(vertexProperty.key())
                .map(key -> this.ont.property(key))
                .filter(Optional::isPresent)
                .filter(property -> !property.get().getpType().equals(this.typeProperty.getpType()))
                .map(property -> new Property(property.get().getpType(), RAW, vertexProperty.value()))
                .toJavaOptional();
    }
    //endregion

    //region Fields
    protected Ontology.Accessor ont;
    protected Map<String, EEntityBase> eEntityBases;
    protected Map<String, Tuple3<EEntityBase, Rel, EEntityBase>> eRels;

    protected org.opensearch.graph.model.ontology.Property typeProperty;

    boolean includeEntities;
    boolean includeRelationships;
    //endregion
}
