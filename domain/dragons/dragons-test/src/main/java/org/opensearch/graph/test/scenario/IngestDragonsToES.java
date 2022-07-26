package org.opensearch.graph.test.scenario;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.action.bulk.BulkProcessor;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestDragonsToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = ETLUtils.getClient();
        BulkProcessor processor = ETLUtils.getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\dragons.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("name", CsvSchema.ColumnType.STRING)
                        .addColumn("power", CsvSchema.ColumnType.NUMBER)
                        .addColumn("gender", CsvSchema.ColumnType.STRING)
                        .addColumn("color", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {});

        String index = "dragons";
        String type = ETLUtils.DRAGON;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> dragon = reader.readValue(line);
                String id = ETLUtils.id(type, dragon.remove("id").toString());

                processor.add(new IndexRequest(index, type, id).source(dragon));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }


}
