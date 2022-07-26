package org.opensearch.graph.executor.ontology;





import org.opensearch.graph.executor.ontology.schema.load.GraphDataLoader;
import org.opensearch.graph.model.GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public interface DataTransformer<T,G> {
    T transform(G data, GraphDataLoader.Directive directive);

    class Utils {
        public static final String INDEX = "Index";
        public static final String TYPE = "type";
        public static SimpleDateFormat sdf;

        static {
            //todo load the pattern from the application.conf ${assembly}.storage_dateFormat
            sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }


    }
}
