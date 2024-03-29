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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.GlobalConstants;

import java.util.Objects;

/**
 * Epair represents the connection between two entities in the ontology
 *  - TypeA states the side-A entity type
 *  - sideAIdField states the side-A related (FK) field name as it appears in the connecting table
 *      (the actual field name on the Side-A entity if stated according to that entity's own fieldID - PK )
 *  - TypeB states the side-B entity type
 *  - sideBIdField states the side-B related(FK)  field name as it appears in the connecting table
 *      (the actual field name on the Side-B entity if stated according to that entity's own fieldID - PK )
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EPair {
    /**
     * describes the nature of the relationship
     */
    public enum RelationType {
        ONE_TO_ONE,ONE_TO_MANY,MANY_TO_MANY
    }
    public EPair() {
    }

    public EPair(String eTypeA, String eTypeB) {
        this(String.format("%s->%s",eTypeA,eTypeB),eTypeA,eTypeB);
    }
    public EPair(RelationType type, String eTypeA, String eTypeB) {
        this(String.format("%s->%s",eTypeA,eTypeB),eTypeA,eTypeB);
    }

    public EPair(RelationType type, String eTypeA, String sideAIdField, String eTypeB, String sideBIdField) {
        this(String.format("%s->%s",eTypeA,eTypeB),type,eTypeA,sideAIdField,eTypeB,sideBIdField);
    }

    public EPair(String name, String eTypeA, String eTypeB) {
        this(name,RelationType.ONE_TO_ONE, eTypeA,eTypeB);
    }

    public EPair(String name,RelationType type, String eTypeA, String eTypeB) {
        this.eTypeA = eTypeA;
        this.eTypeB = eTypeB;
        this.type = type;
        this.name = name;
    }

    public EPair(String name,RelationType type, String eTypeA, String sideAIdField, String eTypeB, String sideBIdField) {
        this.name = name;
        this.type = type;
        this.eTypeA = eTypeA;
        this.sideAIdField = sideAIdField;
        this.eTypeB = eTypeB;
        this.sideBIdField = sideBIdField;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String geteTypeA() {
        return eTypeA;
    }

    public void seteTypeA(String eTypeA) {
        this.eTypeA = eTypeA;
    }

    public String geteTypeB() {
        return eTypeB;
    }

    public void seteTypeB(String eTypeB) {
        this.eTypeB = eTypeB;
    }

    public String getSideAIdField() {
        return sideAIdField;
    }

    public void setSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
    }

    public String getSideBIdField() {
        return sideBIdField;
    }

    public void setSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
    }

    public RelationType getType() {
        return type;
    }

    public void setType(RelationType type) {
        this.type = type;
    }

    @JsonIgnore
    public EPair withSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
        return this;
    }

    @JsonIgnore
    public EPair withSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EPair ePair = (EPair) o;
        return
                Objects.equals(name, ePair.name) &&
                Objects.equals(type, ePair.type) &&
                Objects.equals(eTypeA, ePair.eTypeA) &&
                Objects.equals(sideAIdField, ePair.sideAIdField) &
                        Objects.equals(eTypeB, ePair.eTypeB) &
                        Objects.equals(sideBIdField, ePair.sideBIdField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,type,eTypeA, sideAIdField, eTypeB, sideBIdField);
    }

    @Override
    public String toString() {
        return "EPair [name= " + name + ",type= " + type  + ",eTypeA= " + eTypeA + ",sideAId= " + sideAIdField + ", eTypeB = " + eTypeB + ", sideAId = " + sideBIdField + "]";
    }

    @Override
    public EPair clone()  {
        return new EPair(name,type,eTypeA,sideAIdField,eTypeB,sideBIdField);
    }

    //region Fields
    private String name;
    private String eTypeA;
    private String sideAIdField = GlobalConstants.EdgeSchema.SOURCE_ID;
    private String eTypeB;
    private String sideBIdField = GlobalConstants.EdgeSchema.DEST_ID;
    private RelationType type = RelationType.ONE_TO_ONE;
    //endregion
}
