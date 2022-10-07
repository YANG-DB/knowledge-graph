package org.opensearch.graph.model.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OntologyAccessorTest extends TestCase {

    private ObjectMapper mapper = new ObjectMapper();
    private Ontology.Accessor accessor;

    @Override
    public void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("OntologyJsons/Dragons_Ontology_with_double_nesting.json");
        accessor = new Ontology.Accessor(new ObjectMapper().readValue(stream, Ontology.class));
    }

    /**
     * test cascading fields correctly identifies the nested fields for an entity
     */
    @Test
    public void testCascadingByTypeFields() {
        List<String> fieldTypes = accessor.cascadingElementFieldsPType("Person");
        Assert.assertEquals(13, fieldTypes.size());
        Assert.assertTrue(fieldTypes.contains("origin"));
        Assert.assertTrue(fieldTypes.contains("origin.name"));
    }

    /**
     * test cascading fields correctly identifies the nested fields for an entity
     */
    @Test
    public void testCascadingByNameFields() {
        List<String> fieldTypes = accessor.cascadingElementFieldsPName("Person");
        Assert.assertEquals(13, fieldTypes.size());
        Assert.assertTrue(fieldTypes.contains("origin"));
        Assert.assertTrue(fieldTypes.contains("origin.name"));
    }

    @Test
    public void testPTypes() {
        List<String> elements = StreamSupport.stream(accessor.pTypes().spliterator(), false).collect(Collectors.toList());
        Assert.assertEquals(18, elements.size());
        Assert.assertTrue(elements.contains("origin"));
        Assert.assertTrue(elements.contains("origin.name"));
        Assert.assertTrue(elements.contains("dragons"));
        Assert.assertTrue(elements.contains("dragons.name"));
    }

    @Test
    public void testGet() {
        Assert.assertNotNull(accessor.get());
    }

    @Test
    public void testName() {
        Assert.assertEquals("Dragons", accessor.name());
    }

    @Test
    public void test$element() {
        Assert.assertTrue(accessor.$element("Person").isPresent());
        Assert.assertTrue(accessor.$element("Dragon").isPresent());
        Assert.assertTrue(accessor.$element("Horse").isPresent());
        Assert.assertTrue(accessor.$element("Kingdom").isPresent());
        Assert.assertTrue(accessor.$element("Guild").isPresent());
        Assert.assertTrue(accessor.$element("freeze").isPresent());
        Assert.assertTrue(accessor.$element("origin").isPresent());
        Assert.assertTrue(accessor.$element("fire").isPresent());
        Assert.assertTrue(accessor.$element("memberOf").isPresent());
        Assert.assertTrue(accessor.$element("own").isPresent());
        Assert.assertTrue(accessor.$element("subject").isPresent());
        Assert.assertTrue(accessor.$element("registered").isPresent());
    }

    @Test
    public void test$entity() {
        Assert.assertTrue(accessor.$entity("Person").isPresent());
        Assert.assertTrue(accessor.$entity("Dragon").isPresent());
        Assert.assertTrue(accessor.$entity("Horse").isPresent());
        Assert.assertTrue(accessor.$entity("Kingdom").isPresent());
        Assert.assertTrue(accessor.$entity("Guild").isPresent());
    }

    @Test
    public void $directive() {
    }

    @Test
    public void $directive$() {
    }

    @Test
    public void test$entity$() {
        Assert.assertEquals("Person", accessor.$entity$("Person").geteType());
        Assert.assertEquals("Dragon", accessor.$entity$("Dragon").geteType());
        Assert.assertEquals("Horse", accessor.$entity$("Horse").geteType());
        Assert.assertEquals("Kingdom", accessor.$entity$("Kingdom").geteType());
        Assert.assertEquals("Guild", accessor.$entity$("Guild").geteType());
    }

    @Test
    public void entity() {
    }

    @Test
    public void entity$() {
    }

    @Test
    public void eType() {
    }

    @Test
    public void eType$() {
    }

    @Test
    public void $relation() {
        Assert.assertTrue(accessor.$relation("freeze").isPresent());
        Assert.assertTrue(accessor.$relation("origin").isPresent());
        Assert.assertTrue(accessor.$relation("fire").isPresent());
        Assert.assertTrue(accessor.$relation("memberOf").isPresent());
        Assert.assertTrue(accessor.$relation("own").isPresent());
        Assert.assertTrue(accessor.$relation("subject").isPresent());
        Assert.assertTrue(accessor.$relation("registered").isPresent());
    }

    @Test
    public void test$relation$() {
        Assert.assertEquals("freeze", accessor.$relation$("freeze").getName());
        Assert.assertEquals("origin", accessor.$relation$("origin").getName());
        Assert.assertEquals("fire", accessor.$relation$("fire").getName());
        Assert.assertEquals("memberOf", accessor.$relation$("memberOf").getName());
        Assert.assertEquals("own", accessor.$relation$("own").getName());
        Assert.assertEquals("subject", accessor.$relation$("subject").getName());
        Assert.assertEquals("registered", accessor.$relation$("registered").getName());
    }

    @Test
    public void relation() {
    }

    @Test
    public void relation$() {
    }

    @Test
    public void rType() {
    }

    @Test
    public void rType$() {
    }

    @Test
    public void pName() {
    }

    @Test
    public void pNameOrType() {
    }

    @Test
    public void pName$() {
    }

    @Test
    public void pTypeByName() {
    }

    @Test
    public void $pType() {
    }

    @Test
    public void pType$() {
    }

    @Test
    public void test$pType() {
    }

    @Test
    public void pNames() {
    }

    @Test
    public void pTypes() {
    }

    @Test
    public void entities() {
    }

    @Test
    public void containsMetadata() {
    }

    @Test
    public void testNestedEntityFieldName() {
        Assert.assertTrue(accessor.nestedEntityFieldName(accessor.$entity$("Person"),accessor.$entity$("Horse")).isEmpty());
        Assert.assertTrue(accessor.nestedEntityFieldName(accessor.$entity$("Kingdom"),accessor.$entity$("Person")).isEmpty());
        Assert.assertFalse(accessor.nestedEntityFieldName(accessor.$entity$("Person"),accessor.$entity$("Kingdom")).isEmpty());
        Assert.assertFalse(accessor.nestedEntityFieldName(accessor.$entity$("Person"),accessor.$entity$("Dragon")).isEmpty());

        Assert.assertEquals("origin",accessor.nestedEntityFieldName(accessor.$entity$("Dragon"), accessor.$entity$("Kingdom")).get(0).getName());
        Assert.assertEquals("origin",accessor.nestedEntityFieldName(accessor.$entity$("Person"), accessor.$entity$("Kingdom")).get(0).getName());
        Assert.assertEquals("dragons",accessor.nestedEntityFieldName(accessor.$entity$("Person"), accessor.$entity$("Dragon")).get(0).getName());

    }
    @Test
    public void testCascadingElementFields() {
        Assert.assertEquals(1,accessor.generateCascadingElementFields("Kingdom").size());

        Assert.assertEquals(4,accessor.generateCascadingElementFields("Dragon").size());
        Assert.assertTrue(accessor.generateCascadingElementFields("Dragon").stream().anyMatch(p->p.getpType().equals("origin")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Dragon").stream().anyMatch(p->p.getpType().equals("origin.name")));

        Assert.assertEquals(13,accessor.generateCascadingElementFields("Person").size());
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("origin")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("origin.name")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("dragons")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("dragons.name")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("dragons.gender")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("dragons.origin")));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person").stream().anyMatch(p->p.getpType().equals("dragons.origin.name")));

        Assert.assertTrue(accessor.generateCascadingElementFields("Person")
                .stream().anyMatch(p->p.equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom"))));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person")
                .stream().anyMatch(p->p.equals(new Property.NestedProperty("dragons.name","dragons.name","string","Dragon"))));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person")
                .stream().anyMatch(p->p.equals(new Property.NestedProperty("dragons.origin","dragons.origin","Kingdom","Dragon"))));
        Assert.assertTrue(accessor.generateCascadingElementFields("Person")
                .stream().anyMatch(p->p.equals(new Property.NestedProperty("dragons.origin.name","dragons.origin.name","string","Kingdom"))));

        Assert.assertTrue(accessor.generateCascadingElementFields("Dragon")
                .stream().anyMatch(p->p.equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom"))));


    }
    @Test
    public void testNested$() {
        Assert.assertEquals(0, accessor.nested$("Kingdom").size());
        Assert.assertEquals(1, accessor.nested$("Dragon").size());
        Assert.assertEquals(2, accessor.nested$("Person").size());
        Assert.assertEquals("Kingdom", accessor.nested$("Person").get(0).geteType());
        Assert.assertEquals("Dragon", accessor.nested$("Person").get(1).geteType());
    }

    @Test
    public void testNestedParent() {
        accessor = new Ontology.Accessor(OntologyFinalizer.finalize(accessor.get()));
        Assert.assertFalse(accessor.nestedParent("Dragon", "dragons").isPresent());
        Assert.assertFalse(accessor.nestedParent("Dragon", "person.origin.name").isPresent());
        Assert.assertFalse(accessor.nestedParent("Dragon", "dragons.origin.name").isPresent());
        Assert.assertFalse(accessor.nestedParent("Person", "dragons.origin.name").isPresent());
        Assert.assertFalse(accessor.nestedParent("Kingdom", "origin.name").isPresent());

        Assert.assertTrue(accessor.nestedParent("Kingdom", "dragons.origin.name").isPresent());
        Assert.assertEquals("Dragon",accessor.nestedParent("Kingdom", "dragons.origin.name").get().geteType());
    }

    @Test
    public void testGetNestedEntity() {
        Assert.assertFalse(accessor.nestedEntity("Dragon", "name").isPresent());
        Assert.assertFalse(accessor.nestedEntity("Dragon", "id").isPresent());
        Assert.assertFalse(accessor.nestedEntity("Person", "name").isPresent());
        Assert.assertFalse(accessor.nestedEntity("Person", "id").isPresent());
        Assert.assertFalse(accessor.nestedEntity("Person", "origin").isPresent());
        Assert.assertTrue(accessor.nestedEntity("Person", "origin.name").isPresent());
        Assert.assertEquals("Kingdom", accessor.nestedEntity("Person", "origin.name").get().geteType());
    }

    @Test
    public void testCascadingFieldNameOrType() {
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person","id").isPresent());
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person","name").isPresent());
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person","origin").isPresent());
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person",".origin").isPresent());
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person","origin.").isPresent());
        Assert.assertFalse(accessor.cascadingFieldNameOrType("Person","origin.bla").isPresent());
        Assert.assertTrue(accessor.cascadingFieldNameOrType("Person","origin.name").isPresent());

        Assert.assertTrue(accessor.cascadingFieldNameOrType("Person","origin.name")
                .get().equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom")));
        Assert.assertTrue(accessor.cascadingFieldNameOrType("Person","dragons.name")
                .get().equals(new Property.NestedProperty("dragons.name","dragons.name","string","Dragon")));
        Assert.assertTrue(accessor.cascadingFieldNameOrType("Person","dragons.origin")
                .get().equals(new Property.NestedProperty("dragons.origin","dragons.origin","Kingdom","Dragon")));
        Assert.assertTrue(accessor.cascadingFieldNameOrType("Person","dragons.origin.name")
                .get().equals(new Property.NestedProperty("dragons.origin.name","dragons.origin.name","string","Kingdom")));

        Assert.assertTrue(accessor.cascadingFieldNameOrType("Dragon","origin.name")
                .get().equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom")));
    }

    @Test
    public void testCascadingFieldPType() {
        Assert.assertFalse(accessor.cascadingFieldPType("Person","id").isPresent());
        Assert.assertFalse(accessor.cascadingFieldPType("Person","name").isPresent());
        Assert.assertFalse(accessor.cascadingFieldPType("Person","origin").isPresent());
        Assert.assertFalse(accessor.cascadingFieldPType("Person",".origin").isPresent());
        Assert.assertFalse(accessor.cascadingFieldPType("Person","origin.").isPresent());
        Assert.assertFalse(accessor.cascadingFieldPType("Person","origin.bla").isPresent());
        Assert.assertTrue(accessor.cascadingFieldPType("Person","origin.name").isPresent());

        Assert.assertTrue(accessor.cascadingFieldPType("Person","origin.name")
                .get().equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom")));
        Assert.assertTrue(accessor.cascadingFieldPType("Person","dragons.name")
                .get().equals(new Property.NestedProperty("dragons.name","dragons.name","string","Dragon")));
        Assert.assertTrue(accessor.cascadingFieldPType("Person","dragons.origin")
                .get().equals(new Property.NestedProperty("dragons.origin","dragons.origin","Kingdom","Dragon")));
        Assert.assertTrue(accessor.cascadingFieldPType("Person","dragons.origin.name")
                .get().equals(new Property.NestedProperty("dragons.origin.name","dragons.origin.name","string","Kingdom")));

        Assert.assertTrue(accessor.cascadingFieldPType("Dragon","origin.name")
                .get().equals(new Property.NestedProperty("origin.name","origin.name","string","Kingdom")));

    }


    @Test
    public void cascadingFieldPType() {
    }

    @Test
    public void cascadingElementFieldsPName() {
    }

    @Test
    public void cascadingElementFieldsPType() {
    }

    @Test
    public void isNestedEntity() {
    }

    @Test
    public void isNestedField() {
    }

    @Test
    public void eNames() {
    }

    @Test
    public void eTypes() {
    }

    @Test
    public void relations() {
    }

    @Test
    public void relationBySideA() {
    }

    @Test
    public void relationBySideB() {
    }

    @Test
    public void rTypes() {
    }

    @Test
    public void rNames() {
    }

    @Test
    public void primitiveType() {
    }

    @Test
    public void primitiveType$() {
    }

    @Test
    public void getEnumeratedTypes() {
    }

    @Test
    public void enumeratedType() {
    }

    @Test
    public void enumeratedType$() {
    }

    @Test
    public void matchNameToType() {
    }
}