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
import org.opensearch.action.bulk.BulkProcessor;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestPeopleToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = ETLUtils.getClient();
        BulkProcessor processor = ETLUtils.getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\persons.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("lastName", CsvSchema.ColumnType.STRING)
                        .addColumn("gender", CsvSchema.ColumnType.STRING)
                        .addColumn("birthDate", CsvSchema.ColumnType.STRING)
                        .addColumn("deathDate", CsvSchema.ColumnType.STRING)
                        .addColumn("height", CsvSchema.ColumnType.NUMBER)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });

        String index = "people";
        String type = ETLUtils.PERSON;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> person = reader.readValue(line);
                String id = ETLUtils.id(type, person.remove("id").toString());

                person.put("birthDate", ETLUtils.sdf.format(new Date(Long.parseLong(person.get("birthDate").toString()) * 1000)));
                person.put("deathDate", ETLUtils.sdf.format(new Date(Long.parseLong(person.get("deathDate").toString()))));

                processor.add(new IndexRequest(index, type, id).source(person));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }

 }
