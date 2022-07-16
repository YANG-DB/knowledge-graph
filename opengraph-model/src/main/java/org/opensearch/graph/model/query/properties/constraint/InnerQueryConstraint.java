package org.opensearch.graph.model.query.properties.constraint;

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
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opensearch.graph.model.query.Query;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerQueryConstraint extends Constraint implements WhereByFacet {

    private JoinType joinType;
    private Query innerQuery;
    private String tagEntity;
    private String projectedField;

    public InnerQueryConstraint() {
    }

    public InnerQueryConstraint(ConstraintOp op, Query innerQuery, String tagEntity, String projectedField) {
        this(op, null, ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL, innerQuery, tagEntity, projectedField);
    }

    public InnerQueryConstraint(ConstraintOp op, Object expression,JoinType joinType, Query innerQuery, String tagEntity, String projectedField) {
        super(op, expression);
        this.joinType = joinType;
        this.innerQuery = innerQuery;
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
    }

    public Query getInnerQuery() {
        return innerQuery;
    }

    public String getProjectedField() {
        return projectedField;
    }

    public String getTagEntity() {
        return tagEntity;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public InnerQueryConstraint clone() {
        return new InnerQueryConstraint(getOp(),getExpr(),getJoinType(), getInnerQuery(), getTagEntity(), getProjectedField());
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery, String tagEntity, String projectedFields) {
        return new InnerQueryConstraint(op, innerQuery, tagEntity, projectedFields);
    }

    public static InnerQueryConstraint of(ConstraintOp op, Object expression,JoinType joinType, Query innerQuery, String tagEntity, String projectedFields) {
        return new InnerQueryConstraint(op, expression, joinType,innerQuery, tagEntity, projectedFields);
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery) {
        return of(op, innerQuery, "", "");
    }


}
