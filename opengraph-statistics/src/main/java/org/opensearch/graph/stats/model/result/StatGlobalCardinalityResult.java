package org.opensearch.graph.stats.model.result;

/*-
 * #%L
 * opengraph-statistics
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

import org.opensearch.graph.stats.model.enums.DataType;

/**
 * Created by benishue on 24/05/2017.
 */
public class StatGlobalCardinalityResult extends StatResultBase{

    //region Ctors
    public StatGlobalCardinalityResult() {
        super();
    }

    public StatGlobalCardinalityResult(String index, String type, String field, String direction, long count, long cardinality) {
        super(index, type, field, field + "_" + direction, DataType.string, count, cardinality);
        this.direction = direction;
    }

    //endregion

    //region Getter & Setters
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    //endregion

    //region Fields
    private String direction;
    //endregion
}
