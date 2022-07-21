package org.opensearch.graph.executor.ontology.schema.load;



import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataTransformerContext<G> {
    private List<DocumentBuilder> entities;
    private List<DocumentBuilder> relations;
    private Response transformationResponse;
    private ObjectMapper mapper;
    private G container;

    public DataTransformerContext(ObjectMapper mapper) {
        this.entities = new ArrayList<>();
        this.relations = new ArrayList<>();
        this.mapper = mapper;
        this.transformationResponse = new Response("Transformation");
    }


    public DataTransformerContext withContainer(G container) {
        this.container = container;
        return this;
    }

    public DataTransformerContext withEntities(List<DocumentBuilder> collect) {
        //collect errors
        transformationResponse.failure(collect
                .stream()
                .filter(DocumentBuilder::isFailure)
                .map(DocumentBuilder::getError)
                .collect(Collectors.toList()));

        //collect successes
        transformationResponse.success(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .map(DocumentBuilder::getId)
                .collect(Collectors.toList()));

        //collect documents
        entities.addAll(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .collect(Collectors.toList())
        );
        return this;
    }

    public DataTransformerContext withRelations(List<DocumentBuilder> collect) {
        //collect errors
        transformationResponse.failure(collect
                .stream()
                .filter(DocumentBuilder::isFailure)
                .map(DocumentBuilder::getError)
                .collect(Collectors.toList()));

        //collect successes
        transformationResponse.success(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .map(DocumentBuilder::getId)
                .collect(Collectors.toList()));

        //collect documents
        relations.addAll(collect
                .stream()
                .filter(DocumentBuilder::isSuccess)
                .collect(Collectors.toList())
        );
        return this;
    }

    public DocumentBuilder withEntity(DocumentBuilder entity) {
        getEntities().add(entity);
        return entity;
    }

    public DocumentBuilder withRelation(DocumentBuilder relation) {
        getRelations().add(relation);
        return relation;
    }

    public List<DocumentBuilder> getEntities() {
        return entities;
    }

    public List<DocumentBuilder> getRelations() {
        return relations;
    }

    public G getContainer() {
        return container;
    }

    public Response getTransformationResponse() {
        return transformationResponse;
    }
}
