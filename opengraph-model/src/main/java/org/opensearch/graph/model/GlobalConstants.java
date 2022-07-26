package org.opensearch.graph.model;





public class GlobalConstants {
    public static final boolean INCLUDE_AGGREGATION = false;

    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static String ONTOLOGY = "ontology";
    public static String ASSEMBLY = "assembly";
    public static String ID = "id";

    public static String _ALL = "_all";
    public static String _ID = "_id";
    public static String _DOC = "_doc";
    public static final String RAW = "raw";
    public static final String TYPE = "type";

    public static class ProjectionConfigs {
        public static final String PROJECTION = "projection";
        public static final String TAG = "tag";
        public static final String QUERY_ID = "queryId";
        public static final String CURSOR_ID = "cursorId";
        public static final String EXECUTION_TIME = "timestamp";

    }

    public static class FileSystemConfigs {
        public static final String QUERY_STORE_NAME = ".query_store_folder;";
        public static final String QUERY_STORE_NAME_DEFUALT_VALUE = "query";

        public static final String CURSOR_STORE_NAME = ".cursor_store_folder;";
        public static final String CURSOR_STORE_NAME_DEFUALT_VALUE = "cursor";

        public static final String PAGE_STORE_NAME = ".page_store_folder;";
        public static final String PAGE_STORE_NAME_DEFUALT_VALUE = "page";

        public static final String ONTOLOGY_TRANSFORMATION_PROVIDER_NAME = ".ontology_transformation_provider";
        public static final String ONTOLOGY_TRANSFORMATION_PROVIDER_NAME_DEFUALT_VALUE = "transformProvider";

        public static final String INDEX_PROVIDER_NAME = ".index_provider_dir";
        public static final String INDEX_PROVIDER_NAME_DEFUALT_VALUE = "indexProvider";

        public static final String ONTOLOGY_PROVIDER_NAME = ".ontology_provider_dir";
        public static final String ONTOLOGY_PROVIDER_NAME_DEFUALT_VALUE = "ontology";
    }

    public static class ConfigurationKeys {
        public static final String ID_GENERATOR_INDEX_NAME = ".idGenerator_indexName";
        public static final String ID_GENERATOR_INDEX_NAME_DEFUALT_VALUE = ".idgenerator";

    }

    public static class HasKeys {
        public static final String PROMISE = "promise";
        public static final String CONSTRAINT = "constraint";
        public static final String DIRECTION = GlobalConstants.EdgeSchema.DIRECTION;
        public static final String COUNT = "count";
    }

    public static class Labels {
        public static final String PROMISE = "promise";
        public static final String PROMISE_FILTER = "promiseFilter";
        public static final String NONE = "_none_";
    }

    public static class EdgeSchema {
        public static String DIRECTION = "direction";

        public static String SOURCE = "entityA";//formally was source
        public static String SOURCE_ID = "entityA.id";//formally was source.id
        public static String SOURCE_TYPE = "entityA.type";//formally was source.type
        public static String SOURCE_NAME = "entityA.name";//formally was source.name

        public static String DEST = "entityB";//formally was target
        public static String DEST_ID = "entityB.id";//formally was target.id
        public static String DEST_TYPE = "entityB.type";//formally was target.type
        public static String DEST_NAME = "entityB.name";//formally was target.name
    }
}
