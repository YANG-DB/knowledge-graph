
package org.opensearch.graph.datagen.entities;

/*-
 * #%L
 * observability-datagen
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class OntologyEntity {

    public Map<String, Object> fields;
    public String name;
    public String type;
    public int id;

    public OntologyEntity(String name, int idx) {
        this(name,name,idx);
    }

    public OntologyEntity(String type, String name, int idx) {
        this.type = type;
        this.name = name + "_" + idx;
        this.id = idx;
        this.fields = new LinkedHashMap<>();
    }

    public OntologyEntity withField(String name, Object value) {
        this.fields.put(name, value);
        return this;
    }

    public String genString() {
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(genShortString());
        fields.forEach((key, value) -> joiner.add(String.format("%s:%s", key, value.toString())));
        return joiner.toString();

    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String genShortString() {
        return this.id + "," + this.type + "," + this.name;
    }

    /**
     * factory method
     *
     * @param name
     * @param idx
     * @return
     */
    public static OntologyEntity generate(String type, String name, int idx) {
        return new OntologyEntity(type, name, idx);
    }
}
