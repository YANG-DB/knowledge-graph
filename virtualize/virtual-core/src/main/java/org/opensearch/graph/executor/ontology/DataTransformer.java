package org.opensearch.graph.executor.ontology;

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





import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public interface DataTransformer<T,G> {
    T transform(G data, GraphDataLoader.Directive directive);

    class Utils {
        public static final String INDEX = "Index";
        public static final String TYPE = "type";
        public static SimpleDateFormat sdf;

        static {
            //todo load the pattern from the application.conf ${assembly}.storage_dateFormat
            sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }


    }
}
