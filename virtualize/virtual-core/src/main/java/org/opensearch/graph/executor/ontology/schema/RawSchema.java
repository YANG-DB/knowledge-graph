package org.opensearch.graph.executor.ontology.schema;


/**
 * Created by lior.perry on 2/11/2018.
 *
 * Describing the elastic (raw) indices & indices partitions
 * each index has id formatting
 */
public interface RawSchema extends IndicesProvider, PartitionResolver {

    String getIdFormat(String type);

    String getIndexPrefix(String type);

    String getIdPrefix(String type);



}
