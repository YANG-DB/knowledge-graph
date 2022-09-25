
package org.opensearch.graph.datagen.storyoficeandfire.entities;

/*-
 * #%L
 * dragons-datagen
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
 *
 * @author shmuel mashiach
 * 
 */
public abstract class OntologyEntity {

    
    public String name ;
    public int id ;
    
    public OntologyEntity(String name,int idx) {
        this.name = name+"_"+String.valueOf(idx);
        this.id = idx;
    }
    
    public abstract String genString();
    
    public abstract String genShortString() ;
}
