package org.opensearch.graph.executor.ontology.schema;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class CachedRawSchema implements RawSchema {
    public static final String rawSchemaParameter = "CachedRawSchema.@rawSchema";
    public static final String systemIndicesParameter = "CachedRawSchema.@systemIndices";

    //region Constructors
    @Inject
    public CachedRawSchema(
            @Named(systemIndicesParameter) IndicesProvider systemIndices,
            @Named(rawSchemaParameter) RawSchema rawSchema) {
        this.indices = new ArrayList<>();
        CollectionUtils.addAll(this.indices, systemIndices.indices());
        CollectionUtils.addAll(this.indices, rawSchema.indices());
        this.rawSchema = rawSchema;

        this.indexPartitions = Collections.synchronizedMap(new HashMap<>());
        this.indexPartitionsPartitions = Collections.synchronizedMap(new HashMap<>());
        this.idFormats = Collections.synchronizedMap(new HashMap<>());
    }
    //endregion

    //region RawSchema Implementation
    @Override
    public IndexPartitions getPartition(String type) {
        return this.indexPartitions.computeIfAbsent(type,
                t -> this.rawSchema.getPartition(t));
    }

    @Override
    public String getIdFormat(String type) {
        return this.idFormats.computeIfAbsent(type, t -> this.rawSchema.getIdFormat(t));
    }

    @Override
    public String getIndexPrefix(String type) {
        return rawSchema.getIndexPrefix(type);
    }

    @Override
    public String getIdPrefix(String type) {
        return rawSchema.getIdPrefix(type);
    }

    @Override
    public List<IndexPartitions.Partition> getPartitions(String type) {
        return this.indexPartitionsPartitions.computeIfAbsent(type, t -> this.rawSchema.getPartitions(t));
    }

    @Override
    public Iterable<String> indices() {
        return this.indices;
    }

    //endregion

    //region Fields
    private RawSchema rawSchema;

    private Map<String, IndexPartitions> indexPartitions;
    private Map<String, List<IndexPartitions.Partition>> indexPartitionsPartitions;
    private Map<String, String> idFormats;

    private List<String> indices;
    //endregion
}
