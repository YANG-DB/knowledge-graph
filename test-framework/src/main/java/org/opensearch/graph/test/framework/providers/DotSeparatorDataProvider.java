package org.opensearch.graph.test.framework.providers;





import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotSeparatorDataProvider implements GenericDataProvider {
    private GenericDataProvider innerProvider;

    public DotSeparatorDataProvider(GenericDataProvider innerProvider) {
        this.innerProvider = innerProvider;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws Exception {
        return Stream.ofAll(innerProvider.getDocuments()).map(doc -> {
            List<String> compositeKeys = Stream.ofAll(doc.keySet()).filter(key -> key.contains(".")).toJavaList();

            Map<String, Object> newValues = new HashMap<>();
            for(String key : compositeKeys){
                Object value = doc.remove(key);
                String[] parts = key.split("\\.");
                if(!newValues.containsKey(parts[0])){
                    newValues.put(parts[0], new HashMap<String, Object>());
                }
                ((HashMap<String, Object>)newValues.get(parts[0])).put(parts[1], value);
            }

            for (Map.Entry<String, Object> entry : newValues.entrySet()){
                doc.put(entry.getKey(), entry.getValue());
            }

            return doc;
        });
    }
}
