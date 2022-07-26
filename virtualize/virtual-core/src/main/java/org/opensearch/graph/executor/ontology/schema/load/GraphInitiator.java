package org.opensearch.graph.executor.ontology.schema.load;





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
