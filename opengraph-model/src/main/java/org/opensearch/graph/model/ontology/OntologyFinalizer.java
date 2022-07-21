package org.opensearch.graph.model.ontology;




import javaslang.collection.Stream;

/**
 * Created by moti on 5/14/2017.
 */
public class OntologyFinalizer {

    public static final String ID_FIELD_PTYPE = "id";
    public static final String TYPE_FIELD_PTYPE = "type";

    public static final String ID_FIELD_NAME = "id";
    public static final String TYPE_FIELD_NAME = "type";

    /**
     * verify mandatory fields ID,TYPE exist for all entities & relations on the ontology
     * @param ontology
     * @return
     */
    public static Ontology finalize(Ontology ontology) {
        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(ID_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE).withType("string").build());

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(TYPE_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(TYPE_FIELD_PTYPE).withPType(TYPE_FIELD_PTYPE).withType("string").build());

        Stream.ofAll(ontology.getEntityTypes())
                .forEach(entityType -> {
                    if (entityType.fields().stream().noneMatch(p -> p.equals(ID_FIELD_PTYPE))) {
                        entityType.getProperties().add(ID_FIELD_PTYPE);
                    }
                    if (entityType.fields().stream().noneMatch(p -> p.equals(TYPE_FIELD_PTYPE))) {
                        entityType.getProperties().add(TYPE_FIELD_PTYPE);
                    }
                });

        Stream.ofAll(ontology.getRelationshipTypes())
                .forEach(relationshipType -> {
                    if (relationshipType.fields().stream().noneMatch(p -> p.equals(ID_FIELD_PTYPE))) {
                        relationshipType.getProperties().add(ID_FIELD_PTYPE);
                    }
                    if (relationshipType.fields().stream().noneMatch(p -> p.equals(TYPE_FIELD_PTYPE))) {
                        relationshipType.getProperties().add(TYPE_FIELD_PTYPE);
                    }
                });

        return ontology;
    }
}
