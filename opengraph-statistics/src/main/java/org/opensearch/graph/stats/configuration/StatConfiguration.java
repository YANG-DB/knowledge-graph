package org.opensearch.graph.stats.configuration;

/*-
 * #%L
 * opengraph-statistics
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





import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

public class StatConfiguration {

    private Configuration configuration;

    public StatConfiguration(String configPath) throws Exception {
        configuration = setConfiguration(configPath);
    }

    private synchronized Configuration setConfiguration(String configPath) throws Exception {
        if (configuration != null) {
            return configuration;
        }
        configuration = new PropertiesConfiguration(configPath);

        return configuration;
    }

    public Configuration getInstance() {
        return configuration;
    }
}
