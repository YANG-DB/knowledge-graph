package org.opensearch.graph.model.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class OntologyFinalizerTest extends TestCase {
    private Ontology ontology;
    private Ontology doubleNestedOnt;

    @Override
    public void setUp() throws Exception {
        ontology = new ObjectMapper().readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream("OntologyJsons/Dragons_Ontology_with_nesting.json"), Ontology.class);
        doubleNestedOnt = new ObjectMapper().readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream("OntologyJsons/Dragons_Ontology_with_double_nesting.json"), Ontology.class);
    }

    @Test
    public void testNestedOntologyFinalizer() {
        Assert.assertEquals(14, ontology.getProperties().size());
        Ontology finalize = OntologyFinalizer.finalize(ontology);
        Assert.assertEquals(15, finalize.getProperties().size());
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("origin", new Property("name", "name", "string"))));
    }

    @Test
    public void testDoubleNestedOntologyFinalizer() {
        Assert.assertEquals(14, ontology.getProperties().size());
        Ontology finalize = OntologyFinalizer.finalize(doubleNestedOnt);
        Assert.assertEquals(20, finalize.getProperties().size());
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("origin", new Property("name", "name", "string"))));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons", new Property("name", "name", "string"))));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons.origin", new Property("name", "name", "string"))));
    }
}