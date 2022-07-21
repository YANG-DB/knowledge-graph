package org.opensearch.graph.model.query.quant;

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

import java.util.List;

/**
 * Created by lior.perry on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HQuant extends QuantBase {
    private List<Integer> next;

    public List<Integer> getB() {
        return b;
    }

    public void setB(List<Integer> b) {
        this.b = b;
    }

    //region Fields
    private List<Integer> b;

    @Override
    public List<Integer> getNext() {
        return next;
    }

    @Override
    public void setNext(List<Integer> next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }
//endregion
}
