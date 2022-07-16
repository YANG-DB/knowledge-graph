package org.opensearch.graph.model.query.properties;

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

/*-
 *
 * SchematicEProp.java - opengraph-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

/**
 *
 * a calculated field based on tag associated with the entity or entity relation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FunctionRelProp extends RelProp {
    //region Constructors
    public FunctionRelProp() {  }

    public FunctionRelProp(int eNum, String expression, Constraint con) {
        super(eNum, expression, con);
    }
    //endregion

    //region Override Methods
    //endregion

    @Override
    public FunctionRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public FunctionRelProp clone(int eNum) {
        FunctionRelProp clone = new FunctionRelProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    public static FunctionRelProp of(int eNum, String expression, Constraint con ) {
        return new FunctionRelProp(eNum, expression, con);
    }

}
