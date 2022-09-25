package org.opensearch.graph.test.etl;

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



import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.List;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public interface Transformer {
    List<Map<String, String>> transform(List<Map<String, String>> documents);
    CsvSchema getNewSchema(CsvSchema oldSchema);
}
