package org.opensearch.graph.dispatcher.query.rdf;

import org.opensearch.graph.model.ontology.EnumeratedType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.Value;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Ignore
public class OWLPizzaOntologyTranslatorTest {
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        URL pizza = Thread.currentThread().getContextClassLoader().getResource("rdf/pizza.owl");
        OWLToOntologyTransformer transformer = new OWLToOntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        assert pizza != null;

        ontology = transformer.transform("pizza", Collections.singletonList(
                new String(Files.readAllBytes(new File(pizza.toURI()).toPath()))));
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testEnumTranslation() {
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.enumeratedType$("countries"),
                new EnumeratedType("countries",
                        Arrays.asList(new Value(0, "Eurasia"),
                                new Value(1, "NorthAmerica"),
                                new Value(2, "Antarctica"),
                                new Value(3, "Africa"),
                                new Value(4, "SouthAmerica"),
                                new Value(5, "Australia"))));
    }

    @Test
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 18);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
        List<String> expected = Arrays.asList(
                "passwordHash", "authorizations", "currentLoginRemoteAddr",
                "uiPreferences", "username", "createDate", "privileges", "currentLoginDate",
                "previousLoginDate", "emailAddress", "passwordSalt", "loginCount",
                "passwordResetToken", "previousLoginRemoteAddr", "currentWorkspace", "displayName",
                "passwordResetTokenExpirationDate", "status");

        Assert.assertEquals(ontology.getProperties().stream().map(Property::getpType).collect(Collectors.toList()), expected);
    }

    @Test
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 5);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.entity$("Person").geteType(), "Person");
        Assert.assertEquals(accessor.entity$("Thing").geteType(), "Thing");
        Assert.assertEquals(accessor.entity$("Corporation").geteType(), "Corporation");
        Assert.assertEquals(accessor.entity$("img").geteType(), "img");

        Assert.assertEquals(accessor.entity$("user").geteType(), "user");
        Assert.assertEquals(accessor.entity$("user").getProperties().size(), 18);
        Assert.assertEquals(accessor.entity$("user").getMandatory().size(), 0);

    }

    @Test
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.relation$("hasImage").getrType(), "hasImage");
        Assert.assertEquals(accessor.relation$("hasImage").getePairs().size(), 2);
        Assert.assertEquals(accessor.relation$("hasImage").getePairs().get(0).geteTypeA(), "Corporation");
        Assert.assertEquals(accessor.relation$("hasImage").getePairs().get(1).geteTypeA(), "Person");

    }


}
