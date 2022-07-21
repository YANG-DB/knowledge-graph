package org.opensearch.graph.model.execution.plan.composite.descriptors;

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



import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.entity.ETyped;

/**
 * Created by Roman on 3/13/2018.
 */
public class EntityOpDescriptor implements Descriptor<EntityOp> {
    //region Descriptor Implementation
    @Override
    public String describe(EntityOp item) {
        return ETyped.class.isAssignableFrom(item.getAsgEbase().geteBase().getClass()) ?
            String.format("%s(%s(%s))",
                    item.getClass().getSimpleName(),
                    ((ETyped)item.getAsgEbase().geteBase()).geteType(),
                    item.getAsgEbase().geteBase().geteNum()) :
                String.format("%s(%s)",
                        item.getClass().getSimpleName(),
                        item.getAsgEbase().geteBase().geteNum());
    }
    //endregion
}
