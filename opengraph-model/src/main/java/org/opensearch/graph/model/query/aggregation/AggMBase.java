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






/**
 * Created by lior.perry on 19/02/2017.
 */
public abstract class AggMBase extends AggBase {
    public AggMBase(int eNum) {
        super(eNum);
    }

    //region Properties
    public int getN() {
        return this.n;
    }

    public void setN(int value) {
        this.n = value;
    }

    public AggMOp getOp() {
        return this.op;
    }

    public void setOp(AggMOp value) {
        this.op = value;
    }
    //endregion

    //region Fields
    private int n;
    private AggMOp op;
    //endregion
}
