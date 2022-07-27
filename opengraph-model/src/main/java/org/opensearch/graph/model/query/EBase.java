package org.opensearch.graph.model.query;

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







import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensearch.graph.model.query.aggregation.Agg;
import org.opensearch.graph.model.query.aggregation.CountComp;
import org.opensearch.graph.model.query.combiner.EComb;
import org.opensearch.graph.model.query.combiner.RComb;
import org.opensearch.graph.model.query.entity.*;
import org.opensearch.graph.model.query.optional.OptionalComp;
import org.opensearch.graph.model.query.quant.HQuant;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.properties.*;


/**
 * Created by lior.perry on 16/02/2017.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Start", value = Start.class),
        @JsonSubTypes.Type(name = "EConcrete", value = EConcrete.class),
        @JsonSubTypes.Type(name = "EAgg", value = EAgg.class),
        @JsonSubTypes.Type(name = "EComb", value = EComb.class),
        @JsonSubTypes.Type(name = "EProp", value = EProp.class),
        @JsonSubTypes.Type(name = "EPropGroup", value = EPropGroup.class),
        @JsonSubTypes.Type(name = "ETyped", value = ETyped.class),
        @JsonSubTypes.Type(name = "EUntyped", value = EUntyped.class),
        @JsonSubTypes.Type(name = "HQuant", value = HQuant.class),
        @JsonSubTypes.Type(name = "Quant1", value = Quant1.class),
        @JsonSubTypes.Type(name = "RComb", value = RComb.class),
        @JsonSubTypes.Type(name = "Rel", value = Rel.class),
        @JsonSubTypes.Type(name = "RelPattern", value = RelPattern.class),
        @JsonSubTypes.Type(name = "TypedEndPattern", value = TypedEndPattern.class),
        @JsonSubTypes.Type(name = "UnTypedEndPattern", value = UnTypedEndPattern.class),
        @JsonSubTypes.Type(name = "RelProp", value = RelProp.class),
        @JsonSubTypes.Type(name = "RedundantRelProp", value = RedundantRelProp.class),
        @JsonSubTypes.Type(name = "RedundantSelectionRelProp", value = RedundantSelectionRelProp.class),
        @JsonSubTypes.Type(name = "RelPropGroup", value = RelPropGroup.class),
        @JsonSubTypes.Type(name = "Agg", value = Agg.class),
        @JsonSubTypes.Type(name = "OptionalComp", value = OptionalComp.class),
        @JsonSubTypes.Type(name = "CountComp", value = CountComp.class),
        @JsonSubTypes.Type(name = "SchematicEProp", value = SchematicEProp.class),
        @JsonSubTypes.Type(name = "FunctionRelProp", value = FunctionRelProp.class),
        @JsonSubTypes.Type(name = "FunctionEProp", value = FunctionEProp.class),
        @JsonSubTypes.Type(name = "SchematicRankedEProp", value = SchematicRankedEProp.class),
        @JsonSubTypes.Type(name = "ScoreEProp", value = ScoreEProp.class),
        @JsonSubTypes.Type(name = "CalculatedEProp", value = CalculatedEProp.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EBase {
    //region Constructor
    public EBase() {}

    public EBase(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Properties
    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EBase eBase = (EBase) o;

        return eNum == eBase.eNum;
    }

    public EBase clone() {
        return new EBase(eNum);
    }

    public EBase clone(int eNum) {
        return new EBase(eNum);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.geteNum() + ")";
    }

    @Override
    public int hashCode() {
        return this.eNum;
    }
    //endregion

    //region Fields
    private int eNum;
    //endregion

}
