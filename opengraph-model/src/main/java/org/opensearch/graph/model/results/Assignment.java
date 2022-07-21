package org.opensearch.graph.model.results;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.opensearch.graph.model.logical.Edge;
import org.opensearch.graph.model.logical.Vertex;
import javaslang.collection.Stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonSubTypes({
        @JsonSubTypes.Type(name = "Assignment", value = AssignmentCount.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment<E extends Vertex, R extends Edge> {
    private static HashFunction hashFunction = Hashing.murmur3_128();

    //region Constructors
    public Assignment() {
        this.entities = Collections.emptyList();
        this.relationships = Collections.emptyList();
    }
    //endregion

    //region Properties
    public long getId() {
        return hashFunction.hashBytes(toString().getBytes()).asLong();
    }

    public List<R> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<R> relationships) {
        this.relationships = relationships;
    }

    public List<E> getEntities() {
        return entities;
    }

    public void setEntities(List<E> entities) {
        this.entities = entities;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return "Assignment [relationships = " + relationships + ", entities = " + entities + "]";
    }

    //endregion

    //region Fields
    private List<E> entities;
    private List<R> relationships;

    @JsonIgnore
    public Optional<E> getEntityById(String id) {
        return entities.stream().filter(e -> e.id().equals(id)).findAny();
    }

    @JsonIgnore
    public Optional<E> getEntityByTag(String tag) {
        return entities.stream().filter(e -> e.tag().equals(tag)).findAny();
    }

    @JsonIgnore
    public Optional<R> getRelationByTag(String tag) {
        return relationships.stream().filter(e -> e.tag().equals(tag)).findAny();
    }

    @JsonIgnore
    public List<R> getRelationBySource(String source) {
        return relationships.stream().filter(e -> e.source().equals(source)).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<R> getRelationByTarget(String target) {
        return relationships.stream().filter(e -> e.target().equals(target)).collect(Collectors.toList());
    }
    //endregion

    public static final class Builder<E extends Vertex, R extends Edge> {
        private E currentNode;
        private R currentEdge;

        //region Constructors
        private Builder() {
            //entities = new HashMap<>();
            entities = new HashMap<>();
            relationships = new ArrayList<>();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder<>();
        }
        //endregion

        //region Public Methods
        public Builder withEntities(List<E> entities) {
            entities.forEach(this::withEntity);
            return this;
        }

        public Builder withEntity(E entity) {
            E currentEntity = this.entities.get(entity.id());
            if (currentEntity != null) {
                entity.merge(currentEntity);
            }

            entities.put(entity.id(), entity);
            this.currentNode = entity;
            return this;
        }

        public Builder withEntity(E entity, String tag) {
            E currentEntity = this.entities.get(entity.id());
            if (currentEntity != null) {
                entity.merge(currentEntity);
            }

            entities.put(entity.id(), entity);
            this.currentNode = entity;
            return this;
        }

        public Builder withRelationship(R relationship) {
            this.relationships.add(relationship);
            this.currentEdge = relationship;
            return this;
        }

        public Builder withRelationships(List<R> relationships) {
            this.relationships.addAll(relationships);
            return this;
        }


        public E getCurrentNode() {
            return currentNode;
        }

        public R getCurrentEdge() {
            return currentEdge;
        }

        public Assignment<E, R> build() {
            Assignment<E, R> assignment = new Assignment<>();
            //assignment.setEntities(Stream.ofAll(entities.values()).toJavaList());
            assignment.setEntities(Stream.ofAll(this.entities.values()).sortBy(E::label).toJavaList());
            assignment.setRelationships(this.relationships);
            return assignment;
        }
        //endregion

        //region Fields
        private Map<String, E> entities;
        private List<R> relationships;

        //endregion
    }


}
