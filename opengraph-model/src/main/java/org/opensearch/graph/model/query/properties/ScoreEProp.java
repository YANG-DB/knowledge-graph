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



import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.projection.Projection;

/**
 * Eprop with a boost to rank the query results according to the desired boost
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScoreEProp extends EProp implements RankingProp {
    //region Constructors

    public ScoreEProp() {}

    public ScoreEProp(EProp eProp, long boost) {
        this(eProp.geteNum(),eProp.getpType(),eProp.getCon(),boost);
    }

    public ScoreEProp(int eNum, String pType, Constraint con, long boost) {
        super(eNum, pType, con);
        this.boost = boost;
    }

    public ScoreEProp(int eNum, String pType, Projection proj, long boost) {
        super(eNum, pType, proj);
        this.boost = boost;
    }

    @Override
    public ScoreEProp clone() {
        return clone(geteNum());
    }

    @Override
    public ScoreEProp clone(int eNum) {
        ScoreEProp clone = new ScoreEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    //endregion
    public long getBoost() {
        return boost;
    }
    //region Properties

    //region Fields
    private long boost;
    //endregion

}
