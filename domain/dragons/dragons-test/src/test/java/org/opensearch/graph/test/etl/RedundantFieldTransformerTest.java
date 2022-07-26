package org.opensearch.graph.test.etl;

import org.junit.Ignore;
import org.junit.Test;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public class RedundantFieldTransformerTest {

    @Test
    @Ignore
    public void test() throws Exception {
        /*ElasticEmbeddedNode embeddedNode = new ElasticEmbeddedNode(new MappingFileConfigurer("dragons", "src\\test\\resources\\dragon_mapping.json"));
        FileCsvDataProvider csvDataProvider = new FileCsvDataProvider("C:\\Users\\moti\\Downloads\\data\\dragonsTEST.csv",
                CsvSchema.builder().addColumn("id").addColumn("name").addColumn("age").addColumn("gender").addColumn("color").build());
        SearchEngineDataPopulator populator = new SearchEngineDataPopulator(embeddedNode.getClient(), "dragons","dragon","id", csvDataProvider);
        populator.populate();*/

        Settings settings = Settings.builder().put("cluster.name", "fuse-test").build();
        TransportClient transportClient = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("52.166.57.208"), 9300));

        Map<String, String> fields = new HashMap<>();
        fields.put("name", GlobalConstants.EdgeSchema.DEST_NAME);

        //RedundantFieldTransformer transformer = new RedundantFieldTransformer(transportClient, fields, "EntityB.Id", "id", Arrays.asList("dragons"), "Dragon");

        List<Map<String,String>> documents = new ArrayList<>();
        Map<String, String> doc = new HashMap<>();

        doc.put("id","Fire123");
        doc.put("EntityA.Id","Dragon__7");
        doc.put("EntityB.Id","Dragon_1");
        doc.put("time", "10000");
        doc.put("duration","10");
        documents.add(doc);

        //transformer.transform(documents);
    }
}

