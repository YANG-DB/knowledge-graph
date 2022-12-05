package org.opensearch.graph.generator.configuration;

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


import java.util.Properties;
import java.util.function.Consumer;

public class EntityConfigurationBase {

    //region Ctrs

    public EntityConfigurationBase(int numberOfNodes,
                                   int edgesPerNode,
                                   String entitiesFilePath,
                                   String relationsFilePath) {
        this(new Properties(), numberOfNodes, edgesPerNode, entitiesFilePath, relationsFilePath);
    }

    public EntityConfigurationBase(
            Properties settings,
            int numberOfNodes,
            int edgesPerNode,
            String entitiesFilePath,
            String relationsFilePath) {
        this.settings = settings;
        this.numberOfNodes = numberOfNodes;
        this.edgesPerNode = edgesPerNode;
        this.entitiesFilePath = entitiesFilePath;
        this.relationsFilePath = relationsFilePath;
    }

    public EntityConfigurationBase(
            Properties settings,
            int numberOfNodes,
            int edgesPerNode) {
        this.settings = settings;
        this.numberOfNodes = numberOfNodes;
        this.edgesPerNode = edgesPerNode;
    }
    //endregion

    //region Getters
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getEdgesPerNode() {
        return edgesPerNode;
    }

    public String getEntitiesFilePath() {
        return entitiesFilePath;
    }

    public String getRelationsFilePath() {
        return relationsFilePath;
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return this.settings.getOrDefault(key, defaultValue);
    }

    public Properties getSettings() {
        return settings;
    }

    public PropertiesBuilder withProperty(String key) {
        if (!settings.contains(key)) {
            settings.put(key, new Properties());
        }

        if (settings.containsKey(key) && (settings.get(key) instanceof Properties))
            return new PropertiesBuilder(this,(Properties) settings.get(key));

        return new PropertiesBuilder(this,null);
    }

    public static class PropertiesBuilder {
        private Properties parent;
        private EntityConfigurationBase root;

        public PropertiesBuilder(EntityConfigurationBase root, Properties parent) {
            this.root = root;
            this.parent = parent;
        }

        public boolean accessible() {
            return parent!=null;
        }

        /**
         * add a property to a parent type property (parent entity is a properties map by itself)
         *
         * @param key
         * @param value
         * @return
         */
        public PropertiesBuilder addProperty(String key, Object value) {
            parent.put(key, value);
            return this;
        }

        public Properties build() {
            return parent;
        }

        public EntityConfigurationBase $() {
            return root;
        }

        public PropertiesBuilder ifPresent(Consumer<PropertiesBuilder> action) {
            if (accessible()) {
                action.accept(this);
            }
            return this;
        }

    }
//endregion

    //region Fields
    private int numberOfNodes;
    private int edgesPerNode;
    private String entitiesFilePath;
    private String relationsFilePath;
    private Properties settings;


    //endregion

}



