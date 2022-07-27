package org.opensearch.graph.test.scenario;

/*-
 * #%L
 * dragons-test
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.action.bulk.BulkProcessor;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.common.unit.ByteSizeUnit;
import org.opensearch.common.unit.ByteSizeValue;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.transport.client.PreBuiltTransportClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 07/06/2017.
 */
public class IngestFreezeToES {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClientDataOnly();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\edges\\dragonsRelations_FREEZES_chunks\\dragonsRelations_FREEZES-out.200012.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });

        String index = "dp200012";
        String type = "freeze";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> fire = reader.readValue(line);
                String id = fire.remove("id").toString();

                fire.put(GlobalConstants.EdgeSchema.DIRECTION, fire.get(GlobalConstants.EdgeSchema.DIRECTION).toString().toUpperCase());

                Map<String, Object> entityA = new HashMap<>();
                entityA.put("type", fire.remove(GlobalConstants.EdgeSchema.SOURCE_TYPE));
                entityA.put("id", entityA.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.SOURCE_ID));
                entityA.put("name", fire.remove(GlobalConstants.EdgeSchema.SOURCE_NAME));

                Map<String, Object> entityB = new HashMap<>();
                entityB.put("type", fire.remove(GlobalConstants.EdgeSchema.DEST_TYPE));
                entityB.put("id", entityB.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.DEST_ID));
                entityB.put("name", fire.remove(GlobalConstants.EdgeSchema.DEST_NAME));

                fire.put(GlobalConstants.EdgeSchema.SOURCE, entityA);
                fire.put(GlobalConstants.EdgeSchema.DEST, entityB);

                processor.add(new IndexRequest(index, type, id)
                        .source(fire)
                        .routing(entityA.get("id").toString()));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }

    private static Client getClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "fuse-test").build();
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("52.174.90.109"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.93.93.10"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.93.93.190"), 9300));
    }

    private static Client getClientDataOnly() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "fuse-test").build();
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("52.166.57.208"), 9300));
    }

    private static BulkProcessor getBulkProcessor(Client client) {
        return BulkProcessor.builder(client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long l, BulkRequest bulkRequest) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                        int x = 5;
                    }
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                        int x = 5;
                    }
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();
    }
}
