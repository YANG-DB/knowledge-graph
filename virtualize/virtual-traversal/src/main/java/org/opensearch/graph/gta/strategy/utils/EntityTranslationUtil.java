package org.opensearch.graph.gta.strategy.utils;

/*-
 * #%L
 * virtual-traversal
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





import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EntityTranslationUtil {
    public static List<String> getValidEntityNames(Ontology.Accessor ont, EEntityBase entity) {
        if (entity instanceof EConcrete) {
            return Collections.singletonList(ont.$entity$(((EConcrete) entity).geteType()).getName());
        } else if (entity instanceof ETyped) {
            return Collections.singletonList(ont.$entity$(((ETyped) entity).geteType()).getName());
        } else if (entity instanceof EUntyped) {
            return getValidEntityNames(ont, (EUntyped)entity);
        }

        return Collections.emptyList();
    }

    public static List<String> getValidEntityNames(Ontology.Accessor ont, EUntyped eUntyped) {
        List<String> eTypeNames = Stream.ofAll(eUntyped.getvTypes() == null ?
                Collections.emptyList() :
                eUntyped.getvTypes())
                .map(eType -> ont.$entity$(eType).getName())
                .toJavaList();

        if (eTypeNames.isEmpty()) {
            Set<String> nvTypeNames = Stream.ofAll(eUntyped.getNvTypes() == null ?
                    Collections.emptyList() :
                    eUntyped.getNvTypes())
                    .map(eType -> ont.$entity$(eType).getName())
                    .toJavaSet();

            eTypeNames = Stream.ofAll(ont.entities())
                    .map(EntityType::getName)
                    .filter(eName -> !nvTypeNames.contains(eName))
                    .toJavaList();
        }

        if (eTypeNames.isEmpty()) {
            eTypeNames = Stream.ofAll(ont.entities())
                    .map(EntityType::getName)
                    .toJavaList();
        }

        return eTypeNames;
    }
}
