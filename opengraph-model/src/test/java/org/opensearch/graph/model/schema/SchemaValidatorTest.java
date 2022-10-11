package org.opensearch.graph.model.schema;

import junit.framework.TestCase;
import org.junit.Test;

public class SchemaValidatorTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    //  verify each top level entity has both ID & TYPE metadata fields
    public void testIsValid_top_level_metadata_entities() {
        //todo implement
    }
    //  verify each top level entity has both ID & TYPE metadata fields
    public void testIsValid_top_level_metadata_relations() {
        //todo implement
    }
    //  verify each top level entity all its properties
    public void testIsValid_top_level_properties_entities() {
        //todo implement
    }
    //  verify each top level entity all its properties
    public void testIsValid_top_level_properties_relations() {
        //todo implement
    }
    //verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
    public void testIsValid_top_level_nested_entities() {
        //todo implement
    }
    //verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
    public void testIsValid_top_level_nested_relations() {
        //todo implement
    }
    //verify all cascading fields appear in the properties
    public void testIsValid_top_level_nested_properties_entities() {
        //todo implement
    }
    //verify all cascading fields appear in the properties
    public void testIsValid_top_level_nested_properties_relations() {
        //todo implement
    }
    //verify properties has a valid type (primitive or entity type)
    public void testIsValid_properties_valid() {
        //todo implement
    }

    // cascading entities/relations properties verification
    public void testIsValid_cascading_entities_properties_valid() {
        //todo implement
    }

    // cascading entities/relations properties verification
    public void testIsValid_cascading_relations_properties_valid() {
        //todo implement
    }

    //    verify all index entities has corresponding ontology entities
    public void testIsValid_index_entities() {
        //todo implement
    }
    //    verify all index entities has corresponding ontology entities
    public void testIsValid_index_relations() {
        //todo implement
    }
    //    verify all index entities has corresponding ontology entities
    public void testIsValid_index_nested_entities() {
        //todo implement
    }
    //    verify all index entities has corresponding ontology entities
    public void testIsValid_index_nested_relations() {
        //todo implement
    }
    //    verify all index relations has correct redundant valid fields
    public void testIsValid_index_redundant_relations() {
        //todo implement
    }








}