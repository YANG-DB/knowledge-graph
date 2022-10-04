package org.opensearch.graph.executor.ontology.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.junit.Ignore;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.exact;
import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.nested;

public class GraphElementSchemaProviderJsonFactoryTest {
    private ObjectMapper mapper = new ObjectMapper();
    private Ontology ontology;
    private static Config config;
    private static OntologyProvider ontologyProvider;
    private IndexProvider provider;
    private static IndexProviderFactory providerFactory;

    @Before
    public void setUp() throws Exception {

        providerFactory = Mockito.mock(IndexProviderFactory.class);
        when(providerFactory.get(any())).thenAnswer(invocationOnMock -> Optional.of(provider));

        ontologyProvider = Mockito.mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenAnswer(invocationOnMock -> Optional.of(ontology));

        config = Mockito.mock(Config.class);
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Dragons");

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/DragonsIndexProviderDeepNested.conf");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/NestedDragons.json");
        ontology = mapper.readValue(stream, Ontology.class);
    }

    private GraphElementSchemaProviderFactory getFactory() {
        return new GraphElementSchemaProviderJsonFactory(config, providerFactory, ontologyProvider);
    }

    @Test
    public void testGraphElementSchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
    }

    @Test
    public void testGraphElementEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> person = schemaProvider.getVertexSchemas("Person");
        Assert.assertTrue(person.iterator().hasNext());

        GraphVertexSchema personSchema = person.iterator().next();
        Assert.assertEquals("Person", personSchema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(personSchema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(24, properties.size());
        Assert.assertEquals(1, properties.stream().filter(p->p.getType().equals("Profession")).count());
        Assert.assertEquals(14, properties.stream().filter(p->p.getpType().startsWith("profession.")).count());
        Assert.assertEquals(7, properties.stream().filter(p->p.getpType().startsWith("profession.guild.")).count());

    }
    @Test
    public void testGraphElementNestedEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> person = schemaProvider.getVertexSchemas("Profession");
        Assert.assertTrue(person.iterator().hasNext());

        GraphVertexSchema personSchema = person.iterator().next();
        Assert.assertEquals("Profession", personSchema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(personSchema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(1, properties.stream().filter(p->p.getpType().equals("name")).count());

        GraphElementPropertySchema propertySchema = properties.stream().filter(p -> p.getpType().equals("name")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("profession")));
    }
    @Test
    public void testGraphElementCascadedNestedEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> person = schemaProvider.getVertexSchemas("Person");
        Assert.assertTrue(person.iterator().hasNext());

        GraphVertexSchema personSchema = person.iterator().next();
        Assert.assertEquals("Person", personSchema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(personSchema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(14, properties.stream().filter(p->p.getpType().startsWith("profession.")).count());

        GraphElementPropertySchema propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.name")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("profession")));

        propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.guild")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertFalse(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("guild")));
    }
    @Test
    public void testCascadedNestedGraphPropertiesSchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schemaProvider.getPropertyByPTypeSchemas().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(14, properties.stream().filter(p->p.getpType().startsWith("profession.")).count());

        GraphElementPropertySchema propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.name")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("profession")));

        propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.guild")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertFalse(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("guild")));
    }


    @Test
    public void testGraphElementDeepCascadedNestedEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> person = schemaProvider.getVertexSchemas("Person");
        Assert.assertTrue(person.iterator().hasNext());

        GraphVertexSchema personSchema = person.iterator().next();
        Assert.assertEquals("Person", personSchema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(personSchema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(7, properties.stream().filter(p->p.getpType().startsWith("profession.guild.")).count());

        GraphElementPropertySchema propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.guild.url")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertFalse(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("guild")));

        propertySchema = properties.stream().filter(p -> p.getpType().equals("profession.guild.name")).findFirst().get();
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(nested)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getType().equals(exact)));
        Assert.assertTrue(StreamSupport.stream(propertySchema.getIndexingSchemes().spliterator(), false).anyMatch(i->i.getName().equals("guild")));
    }


    @Test
    public void testGraphElementSchemaProviderLabel() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getEdgeLabels().spliterator(), false)
                .collect(Collectors.toSet()), new HashSet<>(Arrays.asList("HasProfession", "Freeze", "Fire", "Own", "SubjectOf", "OriginatedIn", "RegisteredIn", "Know", "MemberOf")));
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getVertexLabels().spliterator(), false)
                .collect(Collectors.toSet()), new HashSet<>(Arrays.asList("Horse", "Guild", "Person", "Dragon", "Kingdom", "Profession")));
    }

    @Test
    public void testGraphElementSchemaProviderVertex() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(5, StreamSupport.stream(schemaProvider.getVertexSchemas().spliterator(), false)
                .filter(p -> p.getIndexPartitions().isPresent())
                .filter(p -> p.getIndexPartitions().get() instanceof StaticIndexPartitions)
                .filter(p -> p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
    }

    @Test
    public void testGraphEdgeSchemaImpl() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(28, StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false).count());
        Arrays.asList("HasProfession", "Freeze", "Fire", "Own", "SubjectOf", "OriginatedIn", "RegisteredIn", "Know", "MemberOf")
                .forEach(label -> {
                    Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                    Assert.assertNotNull(edgeSchemas);
                    GraphEdgeSchema schema = edgeSchemas.iterator().next();
                    switch (schema.getLabel().getName()) {
                        case "Freeze":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Freeze)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Fire":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Fire)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Own":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Own)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "SubjectOf":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(SubjectOf)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "OriginatedIn":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(OriginatedIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "RegisteredIn":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(RegisteredIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "Know":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Know)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "HasProfession":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(HasProfession)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;

                        case "MemberOf":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(MemberOf)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;

                        default:
                            Assert.assertTrue("No other Edge label should exist", false);
                    }
                });
    }

    @Test
    public void testGraphElementSchemaProviderEdge() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(28, StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false)
                .filter(p -> p.getIndexPartitions().isPresent())
                .filter(p -> p.getIndexPartitions().get().getPartitions().iterator().hasNext())
                .count());
        schemaProvider.getEdgeSchemas().forEach(schema -> {
            switch (schema.getLabel().getName()) {
                case "HasProfession":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("HasProfession").spliterator().estimateSize(), 2);
                    break;
                case "Freeze":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Freeze").spliterator().estimateSize(), 2);
                    break;
                case "Fire":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Fire").spliterator().estimateSize(), 2);
                    break;
                case "Own":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Own").spliterator().estimateSize(), 4);
                    break;
                case "SubjectOf":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("SubjectOf").spliterator().estimateSize(), 2);
                    break;
                case "OriginatedIn":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("OriginatedIn").spliterator().estimateSize(), 6);
                    break;
                case "RegisteredIn":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("RegisteredIn").spliterator().estimateSize(), 4);
                    break;
                case "Know":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("Know").spliterator().estimateSize(), 2);
                    break;
                case "MemberOf":
                    Assert.assertEquals(schemaProvider.getEdgeSchemas("MemberOf").spliterator().estimateSize(), 4);
                    break;

                default:
                    Assert.assertTrue("No other Edge label should exist", false);
            }
        });
    }


    /**
     *                         new GraphEdgeSchema.Impl(
     *                                 "fire",
     *                                 new GraphElementConstraint.Impl(__.has(T.label, "fire")),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
     *                                         ))),
     *                                 Optional.of(new GraphEdgeSchema.End.Impl(
     *                                         Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
     *                                         Optional.of("Dragon"),
     *                                         Arrays.asList(
     *                                                 new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, "string"),
     *                                                 new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, "string")
     *                                         ))),
     *                                 Direction.OUT,
     *                                 Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
     *                                 Optional.empty(),
     *                                 Optional.of(new StaticIndexPartitions(Collections.singletonList(FIRE.getName().toLowerCase()))),
     *                                 Collections.emptyList(),
     *                                 Stream.of(endA).toJavaSet())
     */
}
