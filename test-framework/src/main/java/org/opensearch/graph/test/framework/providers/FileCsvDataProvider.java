package org.opensearch.graph.test.framework.providers;

/*-
 * #%L
 * test-framework
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
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileCsvDataProvider implements GenericDataProvider {
    private String filePath;
    private CsvSchema csvSchema;

    public FileCsvDataProvider(String filePath, CsvSchema csvSchema) {
        this.filePath = filePath;
        this.csvSchema = csvSchema;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(new TypeReference<Map<String, Object>>() {
        }).with(this.csvSchema);
        MappingIterator<Map<String, Object>> objectMappingIterator = reader.readValues(new File(filePath));
        return () -> objectMappingIterator;
    }
}
