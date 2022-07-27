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





import org.semanticweb.owlapi.model.IRI;

import java.util.Arrays;

public interface OntologyNameSpace {

    String[] namespaces = new String[] {
            "http://xmlns.com/",
            "http://www.w3.org/",
            "http://yangdb.org/"
    };

    String defaultNameSpace = namespaces[2];

    static boolean inside(String name) {
        return Arrays.stream(namespaces).anyMatch(name::startsWith);
    }

    static String reminder(String name) {
        return IRI.create(name).getRemainder().or(name);
    }
}
