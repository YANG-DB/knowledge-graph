package org.opensearch.graph.executor.ontology.schema;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.schema.SchemaValidator;
import org.opensearch.graph.test.BaseITMarker;

import static org.opensearch.graph.executor.TestSuiteIndexProviderSuite.*;

public class DragonsSchemaValidationIT implements BaseITMarker {
    @Before
    public void setUp() throws Exception {
        setupSchema();
    }

    @Test
    public void testNestedSchemaValid() {
        Assert.assertTrue(new SchemaValidator().validate(nestedProvider,new Ontology.Accessor(ontology)).isValid());
    }

    @Test
    public void testEmbeddedSchemaValid() {
        Assert.assertTrue(new SchemaValidator().validate(embeddedProvider,new Ontology.Accessor(ontology)).isValid());
    }

    @Test
    public void testSingleIndexSchemaValid() {
        Assert.assertTrue(new SchemaValidator().validate(singleIndexProvider,new Ontology.Accessor(ontology)).isValid());
    }

}
