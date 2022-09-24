package org.opensearch.graph.dispatcher.query.graphql;

import org.opensearch.graph.model.ontology.EnumeratedType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.Value;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;

import static org.opensearch.graph.model.ontology.Property.equal;

public class GraphQLOntologyTranslatorTest {
    public static Ontology ontology;
    public static GraphQLSchema graphQLSchema;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream schemaInput = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/starWars.graphql");
        InputStream whereInoput = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/whereSchema.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform(whereInoput, schemaInput);
        graphQLSchema = transformer.getGraphQLSchema();

        Assert.assertNotNull(ontology);
    }

    @Test
    public void testEnumTranslation() {
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.enumeratedType$("Episode"),
                new EnumeratedType("Episode",
                        Arrays.asList(new Value(0, "NEWHOPE"),
                                new Value(1, "EMPIRE"),
                                new Value(2, "JEDI"))));


    }

    @Test
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 6);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertTrue(equal(accessor.pName$("id"), new Property.MandatoryProperty(new Property("id", "id", "ID"))));
        Assert.assertTrue(equal(accessor.pName$("name"), new Property.MandatoryProperty(new Property("name", "name", "String"))));
        Assert.assertTrue(equal(accessor.pName$("appearsIn"), new Property.MandatoryProperty(new Property("appearsIn", "appearsIn", "Episode"))));
        Assert.assertTrue(equal(accessor.pName$("description"), new Property("description", "description", "String")));
        Assert.assertTrue(equal(accessor.pName$("primaryFunction"), new Property("primaryFunction", "primaryFunction", "String")));
        Assert.assertTrue(equal(accessor.pName$("homePlanet"), new Property("homePlanet", "homePlanet", "String")));
    }

    @Test
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 3);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.entity$("Droid").geteType(), "Droid");
        Assert.assertEquals(accessor.entity$("Droid").getProperties().size(), 5);
        Assert.assertEquals(accessor.entity$("Droid").getMandatory().size(), 3);

        Assert.assertEquals(accessor.entity$("Human").geteType(), "Human");
        Assert.assertEquals(accessor.entity$("Human").getProperties().size(), 5);
        Assert.assertEquals(accessor.entity$("Human").getMandatory().size(), 3);

        Assert.assertEquals(accessor.entity$("Character").geteType(), "Character");
        Assert.assertEquals(accessor.entity$("Character").getProperties().size(), 4);
        Assert.assertEquals(accessor.entity$("Character").getMandatory().size(), 3);

    }

    @Test
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 2);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.relation$("owns").getrType(), "owns");
        Assert.assertEquals(accessor.relation$("owns").getePairs().size(), 1);

        Assert.assertEquals(accessor.relation$("friends").getrType(), "friends");
        Assert.assertEquals(accessor.relation$("friends").getePairs().size(), 2);

    }

    @Test
    public void testOntology2GraphQLTransformation() {
        GraphQLSchema targetSchema = new GraphQLToOntologyTransformer().transform(ontology);
        Ontology ontologyTarget = new GraphQLToOntologyTransformer().transform(targetSchema);

        Assert.assertEquals(ontology.getEntityTypes(),ontologyTarget.getEntityTypes());
        Assert.assertEquals(ontology.getRelationshipTypes(),ontologyTarget.getRelationshipTypes());
        Assert.assertEquals(ontology.getProperties(),ontologyTarget.getProperties());
        Assert.assertEquals(ontology.getEnumeratedTypes(),ontologyTarget.getEnumeratedTypes());

        Assert.assertEquals(targetSchema.getQueryType().getFieldDefinitions().size()
                ,graphQLSchema.getQueryType().getFieldDefinitions().size());
        Assert.assertEquals(targetSchema.getAllTypesAsList().size()
                ,graphQLSchema.getAllTypesAsList().size());

    }

}
