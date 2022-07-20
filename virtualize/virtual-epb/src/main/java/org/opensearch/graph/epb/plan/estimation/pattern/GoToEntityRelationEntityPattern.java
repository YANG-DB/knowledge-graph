package org.opensearch.graph.epb.plan.estimation.pattern;

/*-
 * #%L
 * virtual-epb
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

import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

/**
 * Created by Roman on 29/06/2017.
 */
public class GoToEntityRelationEntityPattern extends EntityRelationEntityPattern {
    //region Constructors
    public GoToEntityRelationEntityPattern(GoToEntityOp startGoTo, EntityOp start, EntityFilterOp startFilter, RelationOp rel, RelationFilterOp relFilter, EntityOp end, EntityFilterOp endFilter) {
        super(start, startFilter, rel, relFilter, end, endFilter);
        this.startGoTo = startGoTo;
    }
    //endregion

    //region Properties
    public GoToEntityOp getStartGoTo() {
        return startGoTo;
    }
    //endregion

    //region Fields
    private GoToEntityOp startGoTo;
    //endregion
}
