package org.opensearch.graph.model.execution.plan;

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



import org.opensearch.graph.model.query.properties.constraint.Constraint;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AttachedPropertyFilterOp extends PlanOp {
    //region Constructor
    public AttachedPropertyFilterOp() {

    }

    public AttachedPropertyFilterOp(String propName, Constraint condition) {
        this.propName = propName;
        this.condition = condition;
    }
    //endregion

    //region Properties
    public String getPropName() {
        return this.propName;
    }

    public void setPropName(String value) {
        this.propName = value;
    }

    public Constraint getCondition() {
        return this.condition;
    }

    public void setCondition(Constraint value) {
        this.condition = value;
    }
    //endregion

    //region Fields
    private String propName;
    private Constraint condition;
    //endregion
}
