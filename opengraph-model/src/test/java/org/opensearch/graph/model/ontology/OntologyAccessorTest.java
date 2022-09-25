package org.opensearch.graph.model.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

public class OntologyAccessorTest extends TestCase {

    private ObjectMapper mapper = new ObjectMapper();
    private Ontology.Accessor accessor;

    @Override
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("OntologyJsons/Dragons_Ontology_with_nesting.json");
        accessor = new Ontology.Accessor(new ObjectMapper().readValue(stream, Ontology.class));
    }

    /**
     * test cascading fields correctly identifies the nested fields for an entity
     */
    @Test
    public void testCascadingByTypeFields(){
        List<String> fieldTypes = accessor.cascadingElementFieldsPType("Person");
        Assert.assertEquals(8,fieldTypes.size());
        Assert.assertTrue(fieldTypes.contains("origin"));
        Assert.assertTrue(fieldTypes.contains("origin.name"));
    }
    /**
     * test cascading fields correctly identifies the nested fields for an entity
     */
    @Test
    public void testCascadingByNameFields(){
        List<String> fieldTypes = accessor.cascadingElementFieldsPName("Person");
        Assert.assertEquals(8,fieldTypes.size());
        Assert.assertTrue(fieldTypes.contains("origin"));
        Assert.assertTrue(fieldTypes.contains("origin.name"));
    }
}