package org.opensearch.graph.model.execution.plan.entity;

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
 * EntityFilterOp.java - opengraph-model - yangdb - 2,016
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

import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.execution.plan.Filter;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class EntityFilterOp extends AsgEBasePlanOp<EPropGroup> implements Filter {
    //region Constructors
    public EntityFilterOp() {
        super(new AsgEBase<>());
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase) {
        super(asgEBase);
    }

    public EntityFilterOp(AsgEBase<EPropGroup> asgEBase, AsgEBase<EEntityBase> entity) {
        super(asgEBase);
        this.entity = entity;
    }
    //endregion

    //region Properties
    public AsgEBase<EEntityBase> getEntity() {
        return entity;
    }

    public void setEntity(AsgEBase<EEntityBase> entity) {
        this.entity = entity;
    }
    //endregion

    //region Fields
    private AsgEBase<EEntityBase> entity;
    //endregion
}
