package org.opensearch.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.OntologyGraphElementSchemaProviderFactory;
import org.opensearch.graph.executor.ontology.schema.GraphElementSchemaProviderJsonFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.schema.IndexProvider;
import org.opensearch.graph.unipop.schema.providers.GraphEdgeSchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schema.providers.GraphElementSchemaProvider;
import org.opensearch.graph.unipop.schema.providers.GraphVertexSchema;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.GlobalConstants.Scalars.TEXT;

public class ObservabilityIndexSchemaProviderTest {
    private ObjectMapper mapper = new ObjectMapper();
    private Ontology ontology;
    private static Config config;
    private static OntologyProvider ontologyProvider;
    private static Ontology.Accessor accessor;
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

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ObservabilityIndexProvider.json");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Observability.json");
        ontology = OntologyFinalizer.finalize(mapper.readValue(stream, Ontology.class));
        accessor = new Ontology.Accessor(ontology);
    }

    private GraphElementSchemaProviderFactory getFactory() {
        return new OntologyGraphElementSchemaProviderFactory(
                new GraphElementSchemaProviderJsonFactory(config, providerFactory, ontologyProvider));
    }

    @Test
    public void testGraphElementSchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
    }

    @Test
    public void testLog_MachineEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> log = schemaProvider.getVertexSchemas("Log");
        Assert.assertTrue(log.iterator().hasNext());

        GraphVertexSchema schema = log.iterator().next();
        Assert.assertEquals("Log", schema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(66, properties.size());

        // embedded machine entity fields ( including the metadata Id & Type fields)
        Assert.assertEquals(5, properties.stream().filter(p -> p.getpType().startsWith("machine")).count());
        // no nested schema - machine is an embedded entity
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("machine")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

        Assert.assertEquals(true, properties.stream().anyMatch(p -> p.getpType().equals("machine.os")));
        Assert.assertEquals(1, properties.stream().filter(p -> p.getpType().equals("machine.os"))
                .findFirst().get().getIndexingSchemes().spliterator().estimateSize());

        Assert.assertEquals(TEXT, accessor.pType$("machine.os").getType());// this is why this property has exact index schema
        Assert.assertEquals(true, properties.stream().filter(p -> p.getpType().equals("machine.os"))
                .findFirst().get().getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.exact).isPresent());
    }

    @Test
    public void testLog_MachinePropertiesSchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);

        Assert.assertTrue(schemaProvider.getPropertySchema("machine.os", "Log").isPresent());
        Assert.assertEquals(TEXT, accessor.pType$("machine.os").getType());// this is why this property has exact index schema
        //machine.os field is embedded not nested
        Assert.assertTrue(schemaProvider.getPropertySchema("machine.os", "Log").get()
                .getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.exact).isPresent());
        Assert.assertFalse(schemaProvider.getPropertySchema("machine.os", "Log").get()
                .getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent());
    }

    @Test
    public void testLog_GeoPropertiesSchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schemaProvider.getVertexPropertySchemas().spliterator(), false)
                .collect(Collectors.toList());

        // embedded geo entity (not including the metadata Id & Type fields)
        Assert.assertEquals(7, properties.stream().filter(p -> p.getpType().startsWith("geo")).count());
        // no nested schema - geo is an embedded entity
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("geo")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
        // no exact schema for any geo inner field
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("geo")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.exact).isPresent())
                .count());
    }

    @Test
    public void testLog_GeoEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> log = schemaProvider.getVertexSchemas("Log");
        Assert.assertTrue(log.iterator().hasNext());

        GraphVertexSchema schema = log.iterator().next();
        Assert.assertEquals("Log", schema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(66, properties.size());

        // embedded geo entity  ( including the metadata Id & Type fields)
        Assert.assertEquals(7, properties.stream().filter(p -> p.getpType().startsWith("geo")).count());
        // no nested schema - geo is an embedded entity
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("geo")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
        // no exact schema for any geo inner field
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("geo")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.exact).isPresent())
                .count());
    }

    @Test
    public void testLog_EventEntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> log = schemaProvider.getVertexSchemas("Log");
        Assert.assertTrue(log.iterator().hasNext());

        GraphVertexSchema schema = log.iterator().next();
        Assert.assertEquals("Log", schema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(66, properties.size());

        // nested events entity fields (not including the metadata Id & Type fields)
        Assert.assertEquals(37, properties.stream().filter(p -> p.getpType().startsWith("event")).count());
        // embedded schema - all properties should be embedded
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("event")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

    }

    @Test
    public void testEvent_EntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> event = schemaProvider.getVertexSchemas("Event");
        Assert.assertTrue(event.iterator().hasNext());

        Optional<GraphVertexSchema> schema = StreamSupport.stream(event.spliterator(), false)
                .filter(e -> e.getIndexPartitions().isPresent())
                .filter(e -> (e.getIndexPartitions().get() instanceof StaticIndexPartitions))
                .findFirst();
        Assert.assertTrue(schema.isPresent());
        Assert.assertEquals("Event", schema.get().getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schema.get().getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(36, properties.size());

        // nested events entity fields (not including the metadata Id & Type fields)
        Assert.assertEquals(30, properties.stream().filter(p -> p.getpType().startsWith("attributes")).count());
        // embedded schema - all properties are embedded
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("attributes")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
    }

    @Test
    public void testSpan_EntitySchemaProvider() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Iterable<GraphVertexSchema> span = schemaProvider.getVertexSchemas("Span");
        Assert.assertTrue(span.iterator().hasNext());

        GraphVertexSchema schema = span.iterator().next();
        Assert.assertEquals("Span", schema.getLabel().getName());
        List<GraphElementPropertySchema> properties = StreamSupport.stream(schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        Assert.assertEquals(116, properties.size());

        // nested events entity fields (not including the metadata Id & Type fields)
        Assert.assertEquals(37, properties.stream().filter(p -> p.getpType().startsWith("event")).count());
        // nested schema - all properties should have nested fields (missing only the event type field itself)
        Assert.assertEquals(36, properties.stream().filter(p -> p.getpType().startsWith("event")).
                filter(p -> p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

    }

    @Test
    public void testGraphElementSchemaProviderLabel() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getEdgeLabels().spliterator(), false)
                .collect(Collectors.toSet()), new HashSet<>(Arrays.asList("hasStatus", "hasResources", "hasSpanContent", "hasEvent", "hasInstrumentationLibrary", "hasAttributes", "hasMachine", "hasLocation", "hasLinks", "hasTraceGroupFields")));
        Assert.assertEquals(StreamSupport.stream(schemaProvider.getVertexLabels().spliterator(), false)
                .collect(Collectors.toSet()), new HashSet<>(Arrays.asList("Status", "Geo", "Log", "Attributes", "SpanContent", "Event", "Links", "TraceGroupFields", "Resources", "Span", "Machine", "InstrumentationLibrary")));
    }

    @Test
    @Ignore("TODO implement")
    public void testGraphEdgeSchemaImpl() {
        GraphElementSchemaProviderFactory jsonFactory = getFactory();
        GraphElementSchemaProvider schemaProvider = jsonFactory.get(ontology);
        Assert.assertNotNull(schemaProvider);
        Assert.assertEquals(26, StreamSupport.stream(schemaProvider.getEdgeSchemas().spliterator(), false).count());
        Arrays.asList("hasStatus", "hasResources", "hasSpanContent", "hasEvent", "hasInstrumentationLibrary", "hasAttributes", "hasMachine", "hasLocation", "hasLinks", "hasTraceGroupFields")
                .forEach(label -> {
                    Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(label);
                    Assert.assertNotNull(edgeSchemas);
                    GraphEdgeSchema schema = edgeSchemas.iterator().next();
                    switch (schema.getLabel().getName()) {
                        case "hasMachine":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Freeze)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasStatus":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Freeze)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasResources":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Fire)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasSpanContent":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 3);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 2);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Own)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasEvent":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(SubjectOf)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasInstrumentationLibrary":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(OriginatedIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasAttributes":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(RegisteredIn)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasLocation":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(Know)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;
                        case "hasLinks":
                            Assert.assertEquals(schema.getApplications().size(), 1);
                            Assert.assertEquals(schema.getEndA().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getEndB().get().getRedundantProperties().spliterator().estimateSize(), 1);
                            Assert.assertEquals(schema.getConstraint().getTraversalConstraint().toString(), "[HasStep([~label.eq(HasProfession)])]");
                            Assert.assertEquals(schema.getIndexPartitions().get().getPartitions().spliterator().estimateSize(), 1);
                            break;

                        case "hasTraceGroupFields":
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

}
