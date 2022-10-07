package org.opensearch.graph.unipop.controller.promise.appender;

import org.opensearch.graph.model.ontology.EPair;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.unipop.controller.common.appender.CompositeSearchAppender;
import org.opensearch.graph.unipop.controller.common.appender.ElementGlobalTypeSearchAppender;
import org.opensearch.graph.unipop.controller.common.appender.IndexSearchAppender;
import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.promise.context.PromiseElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.promise.TraversalConstraint;
import org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schema.providers.GraphVertexSchema;
import org.opensearch.graph.unipop.schema.providers.OntologySchemaProvider;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.StepDescriptor;
import org.unipop.query.search.SearchQuery;

import java.util.*;

import static org.opensearch.graph.unipop.controller.utils.SearchAppenderUtil.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 30-Mar-17.
 */
public class CompositeAllAppendersTest {

    @Test
    @Ignore
    //tests should be fixed
    public void testSimpleCompositeAppender() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.has(T.label, "Person"));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        ElementControllerContext context =
                new PromiseElementControllerContext(
                        null,
                        new StepDescriptor(mock(Step.class)),
                        Collections.emptyList(),
                        Optional.of(traversalConstraint),
                        Collections.emptyList(),
                        schemaProvider,
                        ElementType.vertex,
                        searchQuery.getLimit());

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        //Testing the composition of the the above appenders
        CompositeSearchAppender<ElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender));

        //Just Global Appender - nothing should be done - since the traversal contains a "Label"
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertFalse(appendResult);

        //Just Global Appender - nothing should be done beside the index appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender), wrap(indexSearchAppender));
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));

        // Index appender, Global Appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(indexSearchAppender), constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 1);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"term\":{\"_type\":\"Person\"}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }

    @Test
    @Ignore
    //tests should be fixed
    public void testCompositeAppender_No_Label_AND_Statement() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.and(__.has("name", "bubu"), __.has("color", P.within((Collection) Arrays.asList("brown", "red")))));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        ElementControllerContext context =
                new PromiseElementControllerContext(
                        null,
                        new StepDescriptor(mock(Step.class)),
                        Collections.emptyList(),
                        Optional.of(traversalConstraint),
                        Collections.emptyList(),
                        schemaProvider,
                        ElementType.vertex,
                        searchQuery.getLimit());

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        //Testing the composition of the the above appenders
        CompositeSearchAppender<ElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender));

        //One of the appenders should return true
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);

        //Since we didn't specify any Label in the constraint, we should get all vertex types.
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

        //Just Global Appender - nothing should be done beside the index appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender), wrap(indexSearchAppender));
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 3);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        Assert.assertTrue(searchBuilder.getIndices().contains("dragonIndex2"));

        //The query should be the same as above
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

        // Index appender, Global Appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender), wrap(indexSearchAppender), constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 3);
        Assert.assertTrue(searchBuilder.getIndices().contains("personIndex1"));
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":[{\"terms\":{\"_type\":[\"Person\",\"Dragon\"]}},{\"bool\":{\"must\":[{\"term\":{\"name\":\"bubu\"}},{\"terms\":{\"color\":[\"brown\",\"red\"]}}]}}]}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }

    @Test
    @Ignore
    //tests should be fixed
    public void testCompositeAppender_Label_OR_Statement() throws JSONException {

        Ontology ontology = getOntology();
        GraphElementSchemaProvider schemaProvider = getOntologySchemaProvider(ontology);
        TraversalConstraint traversalConstraint = new TraversalConstraint(__.or(__.has(T.label, "Dragon"), __.has("color", "yarok_bakbuk")));
        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);

        ElementControllerContext context = new
                PromiseElementControllerContext(
                null,
                new StepDescriptor(mock(Step.class)),
                Collections.emptyList(),
                Optional.of(traversalConstraint),
                Collections.emptyList(),
                schemaProvider,
                ElementType.vertex,
                searchQuery.getLimit());

        SearchBuilder searchBuilder = new SearchBuilder();

        //Index Appender
        IndexSearchAppender indexSearchAppender = new IndexSearchAppender();
        //Global Appender
        ElementGlobalTypeSearchAppender globalAppender = new ElementGlobalTypeSearchAppender();
        //Element Constraint Search Appender
        ElementConstraintSearchAppender constraintSearchAppender = new ElementConstraintSearchAppender();

        CompositeSearchAppender<ElementControllerContext> compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(globalAppender));

        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        //Since we have a label, the global appender should be skip i.e., appender result is False
        Assert.assertFalse(appendResult);

        // Index appender, Constraint Appender
        compositeSearchAppender = new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all, wrap(indexSearchAppender), constraintSearchAppender);
        appendResult = compositeSearchAppender.append(searchBuilder, context);
        Assert.assertTrue(appendResult);
        Assert.assertTrue(searchBuilder.getIndices().size() == 2);
        HashSet<String> expectedIndicesSet = new HashSet<>(Arrays.asList("dragonIndex1", "dragonIndex2"));
        Assert.assertEquals(expectedIndicesSet, searchBuilder.getIndices());

        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"term\":{\"_type\":\"Dragon\"}},{\"term\":{\"color\":\"yarok_bakbuk\"}}]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);

    }


    //region Private Methods
    private OntologySchemaProvider getOntologySchemaProvider(Ontology ontology) {
        return new OntologySchemaProvider(ontology, new GraphElementSchemaProvider.Impl(
                Arrays.asList(
                        new GraphVertexSchema.Impl(Type.of("Dragon"), new StaticIndexPartitions(Arrays.asList("dragonIndex1", "dragonIndex2"))),
                        new GraphVertexSchema.Impl(Type.of("Person"), new StaticIndexPartitions(Collections.singletonList("personIndex1")))
                ),
                Collections.singletonList(
                        new GraphEdgeSchema.Impl(Type.Unknown, new StaticIndexPartitions(Arrays.asList("edgeIndex1", "edgeIndex2"))))
        ));
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
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
