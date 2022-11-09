package org.opensearch.graph.executor.ontology.schema;

import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.executor.ontology.schema.load.DefaultGraphInitiator;
import org.opensearch.graph.executor.ontology.schema.load.GraphInitiator;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.test.BaseITMarker;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GraphInitiatorIT implements BaseITMarker {


    @Test
    public void testSchema()  {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","own","subjectof","dragon","idx_freeze_1500","idx_fire_2000","kingdom","people","idx_fire_1000","horse","guild","idx_freeze_500","know","registeredin","originatedin","memberof").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }

    @Test
    public void testNestedInit() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(19,initiator.init());
    }

    @Test
    public void testCreateMappings() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(14,initiator.createTemplate("Dragons",nestedProvider.getOntology()));
    }

    @Test
    @Ignore("Remove the existing template mapping before calling the API")
    public void testCreateIndices() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(13,initiator.createIndices("Dragons",nestedProvider.getOntology()));
    }

    @Test
    public void testNestedDrop() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));
        GraphInitiator initiator = new DefaultGraphInitiator(config,client,nestedProviderIfc,ontologyProvider,nestedSchema);
        Assert.assertEquals(19,initiator.drop());
    }


}
