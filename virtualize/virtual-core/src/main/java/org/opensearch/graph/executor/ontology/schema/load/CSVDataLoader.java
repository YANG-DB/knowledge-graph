package org.opensearch.graph.executor.ontology.schema.load;

/*-
 * #%L
 * virtual-core
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





import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.LoadResponse;

import java.io.File;
import java.io.IOException;

public interface CSVDataLoader {

    LoadResponse<String, GraphError> load(String type, String label, File data, GraphDataLoader.Directive directive) throws IOException;

    /**
     * does:
     *      - given string representation of csv file,
     *      - convert into bulk set
     *      - commit to repository
     */
    LoadResponse<String, GraphError> load(String type, String label, String payload, GraphDataLoader.Directive directive) throws IOException;

}
