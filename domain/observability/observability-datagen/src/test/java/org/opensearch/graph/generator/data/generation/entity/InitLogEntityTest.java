package org.opensearch.graph.generator.data.generation.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.graph.datagen.entities.OntologyEntity;
import org.opensearch.graph.generator.configuration.EntityConfigurationBase;
import org.opensearch.graph.model.ontology.Ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.currentTimeMillis;
import static org.opensearch.graph.datagen.utilities.GenerateRandom.GeneratorStrategy.NORMAL;
import static org.opensearch.graph.datagen.utilities.GenerateRandom.*;

public class InitLogEntityTest {
    static Map<String, EntityConfigurationBase> settings;
    static ObjectMapper mapper = new ObjectMapper();
    static Ontology.Accessor accessor;

    @BeforeClass
    public static void setUp() throws IOException {
        settings = Map.of("Span", new EntityConfigurationBase(new Properties(), 50, 10));
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/Observability.json");
        Ontology ontology = mapper.readValue(stream, Ontology.class);
        accessor = new Ontology.Accessor(ontology);
        Assert.assertNotNull(accessor);
    }

    @Test
    public void testInitEntitiesCreatedCorrectlySpanAndLogEntities() {
        InitEntities entitiesFactory = new InitEntities(settings, accessor);
        Map<String, List<OntologyEntity>> entityMap = entitiesFactory.initEntities();
        Assert.assertTrue(entityMap.containsKey("Log"));
        Assert.assertTrue(entityMap.containsKey("Span"));
    }

    @Test
    public void testInitEntitiesCreatedCorrectlySpanEntityNumericAttributes() {
        settings.get("Span")
                .withProperty("droppedAttributesCount")
                .ifPresent(props -> props.addProperty(MIN_VALUE, 0).addProperty(MAX_VALUE, 5))
                .$().withProperty("droppedEventsCount")
                .ifPresent(props -> props.addProperty(MIN_VALUE, 0).addProperty(MAX_VALUE, 3))
                .$().withProperty("droppedLinksCount")
                .ifPresent(props -> props.addProperty(STRATEGY, NORMAL.toString()).addProperty(MIN_VALUE, currentTimeMillis() + 10000).addProperty(MAX_VALUE, currentTimeMillis() + 20000));


        InitEntities entitiesFactory = new InitEntities(settings, accessor);
        Map<String, List<OntologyEntity>> entityMap = entitiesFactory.initEntities();
        Assert.assertTrue(entityMap.containsKey("Span"));
        Assert.assertTrue(entityMap.get("Span").size() > 0);
        OntologyEntity span = entityMap.get("Span").get(0);

        Assert.assertEquals(span.getType(), "Span");
        Assert.assertTrue(span.getName().startsWith("Span"));
        Assert.assertTrue(span.getId() > 0);

        Assert.assertFalse(span.getFields().isEmpty());
        Assert.assertTrue(span.getFields().containsKey("droppedAttributesCount"));
        Assert.assertTrue(span.getFields().containsKey("droppedEventsCount"));
        Assert.assertTrue(span.getFields().containsKey("droppedLinksCount"));
    }

    @Test
    public void testInitEntitiesCreatedCorrectlySpanEntityDateTimeAttributes() {
        settings.get("Span")
                .withProperty("durationInNanos")
                .ifPresent(props -> props.addProperty(STRATEGY, NORMAL.toString()).addProperty(MIN_VALUE, 1000).addProperty(MAX_VALUE, 100000))
                .$().withProperty("startTime")
                .ifPresent(props -> props.addProperty(STRATEGY, NORMAL.toString()).addProperty(MIN_VALUE, currentTimeMillis()).addProperty(MAX_VALUE, currentTimeMillis() + 10000))
                .$().withProperty("endTime")
                .ifPresent(props -> props.addProperty(STRATEGY, NORMAL.toString()).addProperty(MIN_VALUE, currentTimeMillis() + 10000).addProperty(MAX_VALUE, currentTimeMillis() + 20000));


        InitEntities entitiesFactory = new InitEntities(settings, accessor);
        Map<String, List<OntologyEntity>> entityMap = entitiesFactory.initEntities();
        Assert.assertTrue(entityMap.containsKey("Span"));
        Assert.assertTrue(entityMap.get("Span").size() > 0);
        OntologyEntity span = entityMap.get("Span").get(0);

        Assert.assertEquals(span.getType(), "Span");
        Assert.assertTrue(span.getName().startsWith("Span"));
        Assert.assertTrue(span.getId() > 0);

        Assert.assertFalse(span.getFields().isEmpty());
        Assert.assertTrue(span.getFields().containsKey("durationInNanos"));
        Assert.assertTrue(span.getFields().containsKey("startTime"));
        Assert.assertTrue(span.getFields().containsKey("endTime"));

    }
}