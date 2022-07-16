package org.opensearch.graph.model.query.entity;

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
 * EConcrete.java - opengraph-model - yangdb - 2,016
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
import org.opensearch.graph.model.query.EBase;

/**
 * Created by lior.perry on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EConcrete extends ETyped implements Typed.eTyped{
    //region Constructors
    public EConcrete() {}

    public EConcrete(int eNum, String eTag, String eType, String eID, String eName, int next, int b) {
        super(eNum, eTag, eType, next, b);
        this.eID = eID;
        this.eName = eName;
    }

    public EConcrete(int eNum, String eTag, String eType, String eID, String eName, int next) {
        super(eNum, eTag, eType, next);
        this.eID = eID;
        this.eName = eName;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EConcrete eConcrete = (EConcrete) o;

        if (!eID.equals(eConcrete.eID)) return false;
        return eName.equals(eConcrete.eName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eID.hashCode();
        result = 31 * result + eName.hashCode();
        return result;
    }
    //endregion

    //region Properties
    public String geteID() {
        return eID;
    }

    public void seteID(String eID) {
        this.eID = eID;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    protected EConcrete propClone(int eNum, ETyped clone) {
        super.propClone(eNum, clone);
        clone.setNext(getNext());
        clone.setB(getB());
        ((EConcrete)clone).eID = eID;
        ((EConcrete)clone).eName = eName;
        return ((EConcrete)clone);
    }

    @Override
    public EConcrete clone(int eNum) {
        return propClone(eNum,new EConcrete());
    }
    //endregion

    //region Fields
    private String eID;
    private String eName;

    @Override
    public String getTyped() {
        return geteType();
    }
    //endregion
}
