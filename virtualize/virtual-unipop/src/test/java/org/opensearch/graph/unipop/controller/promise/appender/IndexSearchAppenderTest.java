package org.opensearch.graph.unipop.controller.promise.appender;

import org.opensearch.graph.model.ontology.EPair;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.unipop.controller.common.appender.IndexSearchAppender;
import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.schemaProviders.OntologySchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.unipop.query.StepDescriptor;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 29-Mar-17.
 */
public class IndexSearchAppenderTest {

    @Test
    public void appendTest_Constraint_labelDragon() throws Exception {
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Dragon"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0
        );

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(2,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("dragonIndex1"));
        assertTrue(searchBuilder.getIndices().contains("dragonIndex2"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    @Test
    public void appendTest_Constraint_labelPerson() throws Exception {
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Person"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0
        );

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(1,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    @Test
    public void appendTest_Constraint_noLabel() throws Exception {
        //This test should return all the getIndices of the schema
        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has("color","sheker"));

        ElementControllerContext context = new ElementControllerContext.Impl(
                null,
                new StepDescriptor(mock(Step.class)),
                ElementType.vertex,
                schemaProvider,
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                0);

        SearchBuilder searchBuilder = new SearchBuilder();
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        boolean appendResult = indexSearchAppender.append(searchBuilder, context);

        assertEquals(appendResult, true);
        assertEquals(3,searchBuilder.getIndices().size());
        assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        assertFalse(searchBuilder.getIndices().contains("vertexIndex1"));
    }

    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl("Dragon", new StaticIndexPartitions(Arrays.asList("dragonIndex1", "dragonIndex2"))),
                        new GraphVertexSchema.Impl("Person", new StaticIndexPartitions(Collections.singletonList("personIndex1")))
                ),
                Collections.emptyList()
        ));
    }

    private Ontology getOntology() {
        Ontology ontology = mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA("Dragon");
            seteTypeB("Person");
        }});
        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType("Fire").withName("Fire").withEPairs(ePairs).build();
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Person").withName("Person").build());
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("Dragon").withName("Dragon").build());
                    return entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return relTypes;
                }
        );

        return ontology;
    }
    //endregion

}