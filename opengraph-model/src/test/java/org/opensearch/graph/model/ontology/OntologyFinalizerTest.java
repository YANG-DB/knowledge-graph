package org.opensearch.graph.model.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class OntologyFinalizerTest extends TestCase {
    private ObjectMapper mapper = new ObjectMapper();
    private Ontology.Accessor accessor;
    private Ontology ontology;

    @Override
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("OntologyJsons/Dragons_Ontology_with_nesting.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Ontology.Accessor(ontology);
    }

    @Test
    public void testOntologyFinalizer() {
        Assert.assertEquals(14,ontology.getProperties().size());
        Ontology finalize = OntologyFinalizer.finalize(ontology);
        Assert.assertEquals(15,finalize.getProperties().size());
        Assert.assertTrue(finalize.getProperties()
                .contains(new Property.NestedProperty("kingdom",new Property("name","name","string"))));
    }
}