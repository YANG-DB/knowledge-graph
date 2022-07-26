package org.opensearch.graph.assembly.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.ResultResourceInfo;
import org.opensearch.graph.test.BaseITMarker;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.opensearch.graph.assembly.Setup.GraphClient;

public class DragonsSimpleFileUploadIT implements BaseITMarker {
    public static final String DRAGONS = "Dragons";

    private static SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);

    @BeforeClass
    public static void setup() throws Exception {
//        Setup.setup(); //todo remove remark when running IT tests
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() throws Exception {
//        Setup.cleanup();
    }

    @Test
    public void testLoadLogicalGraph() throws IOException, URISyntaxException {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        Map map = new ObjectMapper().readValue(GraphClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(),"indices created:19");

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/LogicalDragonsGraph.json");
        ResultResourceInfo<String> info = GraphClient.uploadGraphFile(DRAGONS, stream);
        Assert.assertFalse(info.isError());

        map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2,((List)map.get("responses")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(0)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(0)).get("successes")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(1)).get("successes"));
        Assert.assertEquals(64,((List)((Map)((List)map.get("responses")).get(1)).get("successes")).size());
    }

    @Test
    public void testLoadDragonsEntitiesCsv() throws IOException, URISyntaxException {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        Map map = new ObjectMapper().readValue(GraphClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(),"indices created:19");

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/Dragons.csv");
        ResultResourceInfo<String> info = GraphClient.uploadCsvFile(DRAGONS,"Entity","Dragon" , stream);
        Assert.assertFalse(info.isError());

        map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2,((List)map.get("responses")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(0)).get("successes"));
        Assert.assertEquals(3,((List)((Map)((List)map.get("responses")).get(1)).get("successes")).size());
    }

    @Test
    public void testLoadFireRelationsCsv() throws IOException, URISyntaxException {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        Assert.assertNotNull(graphResourceInfo);

        Map map = new ObjectMapper().readValue(GraphClient.initIndices(DRAGONS), Map.class);
        Assert.assertEquals(map.get("data").toString().trim(),"indices created:19");

        URL stream = Thread.currentThread().getContextClassLoader().getResource("schema/Fire.csv");
        ResultResourceInfo<String> info = GraphClient.uploadCsvFile(DRAGONS,"Relation" ,"Fire", stream);
        Assert.assertFalse(info.isError());

        map = (Map) new ObjectMapper().readValue(info.getResult(), Map.class).get("data");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2,((List)map.get("responses")).size());
        Assert.assertNotNull(((Map)((List)map.get("responses")).get(0)).get("successes"));
        Assert.assertEquals(4,((List)((Map)((List)map.get("responses")).get(1)).get("successes")).size());
    }
}
