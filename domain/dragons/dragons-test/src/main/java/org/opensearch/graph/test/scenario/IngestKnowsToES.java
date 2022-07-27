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
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;
import org.opensearch.client.transport.TransportClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by Roman on 07/06/2017.
 */
public class IngestKnowsToES {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
//        createIndices("mapping/owns.mapping", "own","own2000", getClient());
        TransportClient client = ETLUtils.getClient();

        IntStream.range(1,13).forEach(p -> {
            try {
                writeToIndex("C:\\demo_data_6June2017\\knows_chunks", "personsRelations_KNOWS-out", "2000" +String.format("%02d", p), client);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    private static void writeToIndex(String folder, String filePrefix, String index, Client client) throws IOException, InterruptedException {
        String type = "know";
        BulkProcessor processor = ETLUtils.getBulkProcessor(client);
        String filePath = Paths.get(folder,filePrefix+"."+index+".csv").toString();
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> fire = reader.readValue(line);
                String id = fire.remove("id").toString();

                fire.put(GlobalConstants.EdgeSchema.DIRECTION, fire.get(GlobalConstants.EdgeSchema.DIRECTION).toString().toUpperCase());

                Map<String, Object> entityA = new HashMap<>();
                entityA.put("type", fire.remove(GlobalConstants.EdgeSchema.SOURCE_TYPE));
                entityA.put("id", entityA.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.SOURCE_ID));
                entityA.put("firstName", fire.remove("entityA.firstName"));

                Map<String, Object> entityB = new HashMap<>();
                entityB.put("type", fire.remove(GlobalConstants.EdgeSchema.DEST_TYPE));
                entityB.put("id", entityB.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.DEST_ID));
                entityB.put("firstName", fire.remove("entityB.firstName"));

                fire.put(GlobalConstants.EdgeSchema.SOURCE, entityA);
                fire.put(GlobalConstants.EdgeSchema.DEST, entityB);

                processor.add(new IndexRequest("pr"+index, type, id)
                        .source(fire)
                        .routing(entityA.get("id").toString()));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
        System.out.println("Completed loading "+index);
    }

}
