package org.opensearch.graph.executor.ontology.schema;





public interface RawSchema extends IndicesProvider, PartitionResolver {

    String getIdFormat(String type);

    String getIndexPrefix(String type);

    String getIdPrefix(String type);



}
