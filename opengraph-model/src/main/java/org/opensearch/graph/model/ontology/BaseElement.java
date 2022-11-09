package org.opensearch.graph.model.ontology;

/*-
 * #%L
 * opengraph-model
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





import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opensearch.graph.model.GlobalConstants;

import java.util.List;
import java.util.StringJoiner;

public interface BaseElement {
    String getName();
    List<String> getIdField();
    List<String> getMetadata();
    List<String> fields();
    List<String> getProperties();

    boolean containsProperty(String key);
    boolean containsMetadata(String key);


        @JsonIgnore
    static String idFieldName(List<String> values) {
        StringJoiner joiner = new StringJoiner("_");
        values.forEach(joiner::add);
        return joiner.toString().length() >0 ?
               joiner.toString() : GlobalConstants.ID;
    }
}
