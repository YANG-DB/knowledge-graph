package org.opensearch.graph.unipop.schema.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensearch.graph.dispatcher.ontology.IndexProviderFactory;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.schema.Entity;
import org.opensearch.graph.model.schema.IndexProvider;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testing Graph vertex provider structure:
 *  For the following ontology: { A, B ,C, Outer}
 *
 *  With the hierarchy structure:
 *
 *  Outer
 *    -- primitive
 *    -- text
 *    -- a (embedded)
 *    -- b (embedded)
 *    -- c (embedded)
 *
 *  A
 *    -- primitive
 *    -- text
 *    -- b (nested)
 *      -- primitive
 *      -- text
 *      -- c (nested)
 *          -- primitive
 *          -- text
 *    -- c (embedded)
 *
 *  B
 *    -- primitive
 *    -- text
 *    -- c (embedded)
 *      -- primitive
 *      -- text
 *
 *  C
 *    -- primitive
 *    -- text
 *
 *  **** Expected Structure: *******
 *
 *  --------------------- TOTAL 4
 *  Outer.ID  - metadata
 *  Outer.TYPE  - metadata
 *
 *  Outer.primitive
 *  Outer.text
 *  -------------------- TOTAL 20
 *  Outer.a
 *  -- Outer.a.ID  - metadata
 *  -- Outer.a.TYPE  - metadata
 *  -- Outer.a.primitive
 *  -- Outer.a.text
 *  -- Outer.a.b
 *  ----- Outer.a.b.ID  - metadata
 *  ----- Outer.a.b.TYPE  - metadata
 *  ----- Outer.a.b.primitive
 *  ----- Outer.a.b.text
 *  ----- Outer.a.b.c
 *  --------- Outer.a.b.c.ID  - metadata
 *  --------- Outer.a.b.c.TYPE  - metadata
 *  --------- Outer.a.b.c.primitive
 *  --------- Outer.a.b.c.text
 *  -- Outer.a.c
 *  ----- Outer.a.c.ID  - metadata
 *  ----- Outer.a.c.TYPE  - metadata
 *  ----- Outer.a.c.primitive
 *  ----- Outer.a.c.text
 *  -------------------- TOTAL 10
 *  Outer.b
 *  -- Outer.b.ID  - metadata
 *  -- Outer.b.TYPE  - metadata
 *  -- Outer.b.primitive
 *  -- Outer.b.text
 *  -- Outer.b.c
 *  ------ Outer.b.c.ID  - metadata
 *  ------ Outer.b.c.TYPE  - metadata
 *  ------ Outer.b.c.primitive
 *  ------ Outer.b.c.text
 *  -------------------- TOTAL 5
 *  Outer.c
 *  --- Outer.c.ID  - metadata
 *  --- Outer.c.TYPE  - metadata
 *  --- Outer.c.primitive
 *  --- Outer.c.text
 *
 */
public class GraphVertexSchemaUtilsTest extends TestCase {

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
        when(config.getString(any())).thenAnswer(invocationOnMock -> "Ontology");

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("IndexProvider.json");
        provider = mapper.readValue(stream, IndexProvider.class);
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Ontology.json");
        ontology = OntologyFinalizer.finalize(mapper.readValue(stream, Ontology.class));
        accessor = new Ontology.Accessor(ontology);
    }

    @Test
    public void testGenerateGraphVertexSchema_Outer_Entity() {
        Assert.assertTrue(provider.getEntity("Outer").isPresent());
        Entity a = provider.getEntity("Outer").get();
        List<GraphVertexSchema> vertexSchemas = GraphVertexSchemaUtils.generateGraphVertexSchema(accessor, provider, a);
        Assert.assertEquals(1,vertexSchemas.size());

        GraphVertexSchema a_Schema = vertexSchemas.iterator().next();
        Assert.assertEquals("Outer", a_Schema.getLabel().getName());

        List<GraphElementPropertySchema> properties = StreamSupport.stream(a_Schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());
        // Outer has 72 properties & 2 metadata (id,type) properties
        Assert.assertEquals(39, properties.size());
        // Outer has 8 primitive fields
        Assert.assertEquals(7, properties.stream().filter(p -> !p.getpType().contains(".")).count());
        // outer has 34 cascading fields derived from A's nested hierarchy
        Assert.assertEquals(19, properties.stream().filter(p -> p.getpType().startsWith("a.")).count());
        // outer has 19 cascading fields derived from A.B's nested hierarchy
        Assert.assertEquals(9, properties.stream().filter(p -> p.getpType().startsWith("b.")).count());
        // outer has 9 cascading fields derived from A.B.C's nested hierarchy
        Assert.assertEquals(4, properties.stream().filter(p -> p.getpType().startsWith("c.")).count());


        Assert.assertEquals(1, properties.stream().filter(p -> p.getpType().equals("a")).count());
        Assert.assertEquals("A", properties.stream().filter(p -> p.getpType().equals("a")).
                findFirst().get().getType());


        Assert.assertEquals(1, properties.stream().filter(p -> p.getpType().equals("b")).count());
        Assert.assertEquals("B", properties.stream().filter(p -> p.getpType().equals("b")).
                findFirst().get().getType());

        // outer.b is embedded
        Assert.assertEquals(1, properties.stream().filter(p -> p.getpType().equals("b")).count());
        Assert.assertEquals("B", properties.stream().filter(p -> p.getpType().equals("b")).
                findFirst().get().getType());

        // outer.c is embedded
        Assert.assertEquals(1, properties.stream().filter(p -> p.getpType().equals("c")).count());
        Assert.assertEquals("C", properties.stream().filter(p -> p.getpType().equals("c")).
                findFirst().get().getType());

    }
    @Test
    public void testGenerateGraphVertexSchema_Outer_Entity_Nesting_Validation() {
        Assert.assertTrue(provider.getEntity("Outer").isPresent());
        Entity outer = provider.getEntity("Outer").get();
        List<GraphVertexSchema> vertexSchemas = GraphVertexSchemaUtils.generateGraphVertexSchema(accessor, provider, outer);
        Assert.assertEquals(1, vertexSchemas.size());

        GraphVertexSchema a_Schema = vertexSchemas.iterator().next();
        Assert.assertEquals("Outer", a_Schema.getLabel().getName());

        //the only nesting index schema is for fields that are nested in A's hierarchy - that is the direct fields for
        // A will be embedded under the outer entity - only A.B*... entities will be nested
        List<GraphElementPropertySchema> properties = StreamSupport.stream(a_Schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());

        // outer.b is embedded
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("b")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

        Assert.assertEquals("B", properties.stream().filter(p -> p.getpType().equals("b")).
                findFirst().get().getType());

        // outer.c is embedded
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("c")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

        Assert.assertEquals("C", properties.stream().filter(p -> p.getpType().equals("c")).
                findFirst().get().getType());

        // outer.outer is embedded
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("a")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());

        Assert.assertEquals("A", properties.stream().filter(p -> p.getpType().equals("a")).
                findFirst().get().getType());

        // under the outer all sub entities are embedded
        Assert.assertEquals(0, properties.stream().
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
    }
    @Test
    public void testGenerateGraphVertexSchema_A_Entity_Nesting_Validation() {
        Assert.assertTrue(provider.getEntity("A").isPresent());
        Entity a = provider.getEntity("A").get();
        List<GraphVertexSchema> vertexSchemas = GraphVertexSchemaUtils.generateGraphVertexSchema(accessor, provider, a);
        Assert.assertEquals(1, vertexSchemas.size());

        GraphVertexSchema a_Schema = vertexSchemas.iterator().next();
        Assert.assertEquals("A", a_Schema.getLabel().getName());


        //the only nesting index schema is for fields that are nested in A's hierarchy - that is the direct fields for
        // B will be embedded under the A entity - only A.B*... entities will be nested
        List<GraphElementPropertySchema> properties = StreamSupport.stream(a_Schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());

        Assert.assertEquals(19, properties.size());

        // a.b is nested
        Assert.assertEquals(10, properties.stream().filter(p -> p.getpType().startsWith("b")).count());
        Assert.assertEquals(9, properties.stream().filter(p -> p.getpType().startsWith("b")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
        Assert.assertEquals("B", properties.stream().filter(p -> p.getpType().equals("b")).
                findFirst().get().getType());

        // a.c is embedded
        Assert.assertEquals(5, properties.stream().filter(p -> p.getpType().startsWith("c")).count());
        Assert.assertEquals("C", properties.stream().filter(p -> p.getpType().equals("c")).
                findFirst().get().getType());
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("c")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());


    }
    @Test
    public void testGenerateGraphVertexSchema_B_Entity_Nesting_Validation() {
        Assert.assertTrue(provider.getEntity("B").isPresent());
        Entity b = provider.getEntity("B").get();
        List<GraphVertexSchema> vertexSchemas = GraphVertexSchemaUtils.generateGraphVertexSchema(accessor, provider, b);
        Assert.assertEquals(1, vertexSchemas.size());

        GraphVertexSchema a_Schema = vertexSchemas.iterator().next();
        Assert.assertEquals("B", a_Schema.getLabel().getName());


        //the only nesting index schema is for fields that are nested in B's hierarchy - that is the direct fields for
        // B will be embedded
        List<GraphElementPropertySchema> properties = StreamSupport.stream(a_Schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());

        Assert.assertEquals(9, properties.size());

        // b.c is embedded
        Assert.assertEquals(5, properties.stream().filter(p -> p.getpType().startsWith("c")).count());
        Assert.assertEquals("C", properties.stream().filter(p -> p.getpType().equals("c")).
                findFirst().get().getType());
        Assert.assertEquals(0, properties.stream().filter(p -> p.getpType().startsWith("c")).
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());


    }
    @Test
    public void testGenerateGraphVertexSchema_C_Entity_Nesting_Validation() {
        Assert.assertTrue(provider.getEntity("C").isPresent());
        Entity c = provider.getEntity("C").get();
        List<GraphVertexSchema> vertexSchemas = GraphVertexSchemaUtils.generateGraphVertexSchema(accessor, provider, c);
        Assert.assertEquals(1, vertexSchemas.size());

        GraphVertexSchema a_Schema = vertexSchemas.iterator().next();
        Assert.assertEquals("C", a_Schema.getLabel().getName());


        //no nesting index schema exist for C
        List<GraphElementPropertySchema> properties = StreamSupport.stream(a_Schema.getProperties().spliterator(), false)
                .collect(Collectors.toList());

        Assert.assertEquals(4, properties.size());

        Assert.assertEquals(0, properties.stream().
                filter(p->p.getIndexingSchema(GraphElementPropertySchema.IndexingSchema.Type.nested).isPresent())
                .count());
    }


}