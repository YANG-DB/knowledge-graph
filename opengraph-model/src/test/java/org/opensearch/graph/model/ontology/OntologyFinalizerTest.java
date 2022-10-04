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
    public void testDoubleNestedOntologyFinalizer() {
        Assert.assertEquals(14, ontology.getProperties().size());
        Ontology finalize = OntologyFinalizer.finalize(doubleNestedOnt);
        Assert.assertEquals(23, finalize.getProperties().size());
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("origin", "origin","Kingdom","Dragon")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("origin", "origin","Kingdom","Person")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons", "dragons","Dragon","Person")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("origin", new Property("name", "name", "string"),"Kingdom")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons", new Property("name", "name", "string"),"Dragon")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons", new Property("gender", "gender", "TYPE_Gender"),"Dragon")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons", new Property("origin", "origin", "Kingdom"),"Dragon")));
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("dragons.origin", new Property("name", "name", "string"),"Kingdom")));
   }
}