package org.opensearch.graph.model.ontology;

/*-
 * #%L
 * opengraph-model
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
import org.opensearch.graph.model.GlobalConstants;

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
     * generate nested fields for all entities & relations
     * @param ontology
     * @return
     */
    public static Ontology finalize(Ontology ontology) {
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(ID_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE)
                    .withType(GlobalConstants.Scalars.STRING).build());

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(TYPE_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(TYPE_FIELD_PTYPE)
                    .withPType(TYPE_FIELD_PTYPE).withType(GlobalConstants.Scalars.STRING).build());

        Stream.ofAll(ontology.getEntityTypes())
                .forEach(entityType -> {
                    // generate nested fields
                    ontology.getProperties().addAll(accessor.generateCascadingElementFields(entityType.geteType()));
                    // add metadata fields
                    if (entityType.fields().stream().noneMatch(p -> p.equals(ID_FIELD_PTYPE))) {
                        entityType.getMetadata().add(ID_FIELD_PTYPE);
                    }
                    if (entityType.fields().stream().noneMatch(p -> p.equals(TYPE_FIELD_PTYPE))) {
                        entityType.getMetadata().add(TYPE_FIELD_PTYPE);
                    }
                });

        Stream.ofAll(ontology.getRelationshipTypes())
                .forEach(relationshipType -> {
                    // generate nested fields
                    ontology.getProperties().addAll(accessor.generateCascadingElementFields(relationshipType.getrType()));
                    // add metadata fields
                    if (relationshipType.fields().stream().noneMatch(p -> p.equals(ID_FIELD_PTYPE))) {
                        relationshipType.getMetadata().add(ID_FIELD_PTYPE);
                    }
                    if (relationshipType.fields().stream().noneMatch(p -> p.equals(TYPE_FIELD_PTYPE))) {
                        relationshipType.getMetadata().add(TYPE_FIELD_PTYPE);
                    }
                });

        return ontology;
    }
}
