package org.opensearch.graph.model.ontology.transformer;

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






import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OntologyTransformer {
    private String ont;
    private List<TransformerEntityType> entityTypes;
    private List<TransformerRelationType> relationTypes;

    public OntologyTransformer() {}

    public OntologyTransformer(String ont, List<TransformerEntityType> entityTypes, List<TransformerRelationType> relationTypes) {
        this.ont = ont;
        this.entityTypes = entityTypes;
        this.relationTypes = relationTypes;
    }

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public List<TransformerEntityType> getEntityTypes() {
        return entityTypes;
    }

    public void setEntityTypes(List<TransformerEntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public List<TransformerRelationType> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<TransformerRelationType> relationTypes) {
        this.relationTypes = relationTypes;
    }
}
