package org.unipop.process.predicate;

/*-
 * #%L
 * unipop-core
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




import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by roman.margolis on 23/11/2017.
 */
public class NotExistsP<V> extends P<V> {
    //region Constructors
    public NotExistsP() {
        super(null, null);
    }
    //endregion

    //region Override Methods
    @Override
    public P<V> negate() {
        return new ExistsP<V>();
    }

    @Override
    public String toString() {
        return "notExists";
    }

    @Override
    public int hashCode() {
        return "notExists".hashCode();
    }
    //endregion
}
