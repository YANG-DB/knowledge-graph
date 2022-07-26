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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.opensearch.graph.test.scenario.ETLUtils.*;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestKingdomsToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClient();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\kingdoms.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("name", CsvSchema.ColumnType.STRING)
                        .addColumn("king", CsvSchema.ColumnType.STRING)
                        .addColumn("queen", CsvSchema.ColumnType.STRING)
                        .addColumn("independenceDay", CsvSchema.ColumnType.STRING)
                        .addColumn("funds", CsvSchema.ColumnType.NUMBER)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {});

        String index = "misc";
        String type = KINGDOM;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> kingdom = reader.readValue(line);
                String id = id(type, kingdom.remove("id").toString());

                kingdom.put("independenceDay", sdf.format(new Date(Long.parseLong(kingdom.get("independenceDay").toString()))));

                kingdom.put("king", kingdom.get("king").toString().replace("King", "").trim());
                kingdom.put("queen", kingdom.get("queen").toString().replace("Queen", "").trim());

                processor.add(new IndexRequest(index, type, id).source(kingdom));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }


}
