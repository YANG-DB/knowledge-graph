package org.opensearch.graph.unipop.schemaProviders.helpers;





import java.text.SimpleDateFormat;

public interface DateFormatterFactory {
    SimpleDateFormat getDateFormatter(String format);
}
