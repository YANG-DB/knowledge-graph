package org.opensearch.graph.services.engine2.data.schema;

import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.IndexPartitions;

import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 2/12/2018.
 */
public class TestRawSchema implements RawSchema {
    @Override
    public IndexPartitions getPartition(String type) {
        return new IndexPartitions.Impl(type);
    }

    @Override
    public String getIdFormat(String type) {
        return "";
    }

    @Override
    public String getIndexPrefix(String type) {
        return "";
    }

    @Override
    public String getIdPrefix(String type) {
        return "";
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<String> indices() {
        return Collections.emptyList();
    }

}
