package org.opensearch.graph.datagen.utilities;

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

import javaslang.collection.Stream;
import org.opensearch.graph.datagen.entities.OntologyEntity;
import org.opensearch.graph.generator.configuration.EntityConfigurationBase;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.javafaker.Faker;

/**
 * entity generator is responsible for creating a fake (mock) entity with fake values for all the attributes
 */
public class EntityGenerator {

    public static final String SIZE = "SIZE";

    /**
     * generate a specific entity
     *
     * @param settings   - the configuration setting for the specific entity (SIZE)
     * @param accessor
     * @param entityType - the entity type
     * @return
     */
    public static List<OntologyEntity> generate(EntityConfigurationBase settings, Ontology.Accessor accessor, EntityType entityType) {
        Context context = new Context(settings,accessor);
        int size = (int) settings.getOrDefault(SIZE, 1000);
        return Stream.rangeClosed(1, size).map(x -> build(settings, context, entityType)).toStream().toArray().toJavaList();

    }

    private static OntologyEntity build(EntityConfigurationBase settings, Context context, EntityType entityType) {
        OntologyEntity entity = new OntologyEntity(entityType.getName(),entityType.geteType(), context.next());
        entityType.getProperties()
                .forEach(p -> entity.withField(p,populateProperty(context,p)));
        return entity;
    }

    private static Optional populateProperty(Context context, String property) {
        if (context.getAccessor().pNameOrType(property).isEmpty())
            return Optional.empty();

        Optional propertyContext = context.getOrDefault(property, Optional.empty());
        Property field = context.getAccessor().pNameOrType(property).get();
        switch (field.getType()) {
            case "string":
                //
                return Optional.of(context.getFaker().name());
            case "date":
                return Optional.of(new Date(GenerateRandom.generateLongProperty(propertyContext,property)));
            case "int":
                return Optional.of(GenerateRandom.generateIntProperty(propertyContext,property));
            case "long":
                return Optional.of(GenerateRandom.generateLongProperty(propertyContext,property));
            case "float":
                return Optional.of(GenerateRandom.generateFloatProperty(propertyContext,property));
        }
        return Optional.empty();
    }

    public static class Context {
        private AtomicInteger index = new AtomicInteger();
        private Ontology.Accessor accessor;
        private Faker faker;
        private EntityConfigurationBase settings;

        public Context(EntityConfigurationBase settings, Ontology.Accessor accessor) {
            this.settings = settings;
            this.accessor = accessor;
            this.faker = new Faker();
        }

        public Properties getSettings() {
            return settings.getSettings();
        }

        public Faker getFaker() {
            return faker;
        }

        public Ontology.Accessor getAccessor() {
            return accessor;
        }

        public int next() {
            return index.incrementAndGet();
        }

        public Optional getOrDefault(String property, Object defaultValue) {
            return Optional.ofNullable(settings.getOrDefault(property,defaultValue));
        }
    }
}
