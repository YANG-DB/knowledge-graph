package org.opensearch.graph.executor.ontology.schema.load;

/*-
 * #%L
 * virtual-core
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





import java.io.IOException;

public interface GraphInitiator {

    long init(String ontology) ;

    /**
     * create the indexTemplates
     * create the vertices and edges indices according to schema
     *
     * @return
     * @throws IOException
     */
    long init() ;

    /**
     * drop the vertices and edges indices to schema
     *
     * @return
     * @throws IOException
     * @param ontology
     */
    long drop(String ontology) ;

    /**
     * drop the vertices and edges indices to schema
     *
     * @return
     * @throws IOException
     */
    long drop() throws IOException;

    /**
     * generate the opensearch index template according to ontology and index schema provider json instructions
     *
     * @param ontology
     * @param schemaProvider
     * @return
     */
    long createTemplate(String ontology, String schemaProvider) ;

    /**
     * generate the opensearch index template according to ontology and index schema provider json instructions
     *
     * @param ontology
     * @return
     */
    long createTemplate(String ontology) ;

    /**
     * create indices according to ontology and index schema provider json instructions
     * @param ontology
     * @param schemaProvider
     * @return
     * @throws IOException
     */
    long createIndices(String ontology, String schemaProvider) ;

   /**
     * create indices according to ontology and index schema provider json instructions
     * @param ontology
     * @return
     * @throws IOException
     */
    long createIndices(String ontology) ;
}
