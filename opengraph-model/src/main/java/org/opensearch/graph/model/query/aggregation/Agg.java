package org.opensearch.graph.model.query.aggregation;

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
 * Agg.java - opengraph-model - yangdb - 2,016
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

import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class Agg extends AggLBase implements Tagged {
    public Agg(int eNum, String eTag,Constraint constraint,  int next) {
        super(eNum);
        seteTag(eTag);
        setCon(constraint);
        setNext(next);
    }

    //region Properties
    public String geteTag() {
        return this.eTag;
    }

    public void seteTag(String value) {
        this.eTag = value;
    }
    //endregion

    //region Fields
    private String eTag;
    //endregion
}
