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



import org.opensearch.graph.model.query.properties.projection.Projection;

/**
 * Created by roman.margolis on 26/09/2017.
 */
public class RedundantSelectionRelProp extends RelProp {
    //region Constructors
    public RedundantSelectionRelProp() {
        super();
    }

    public RedundantSelectionRelProp(int eNum, String pType, String redundantPropName, Projection proj, int b) {
        super(eNum, pType, proj, b);
        this.redundantPropName = redundantPropName;
    }
    //endregion

    @Override
    public RedundantSelectionRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RedundantSelectionRelProp clone(int eNum) {
        final RedundantSelectionRelProp clone = new RedundantSelectionRelProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setB(getB());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    //region Properties
    public String getRedundantPropName() {
        return redundantPropName;
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantSelectionRelProp of(int eNum, String pType, String redundantPropName, Projection proj){
        RedundantSelectionRelProp relProp = new RedundantSelectionRelProp(eNum, pType, redundantPropName, proj, 0);
        relProp.seteNum(eNum);
        relProp.setpType(pType);
        return relProp;
    }
}
