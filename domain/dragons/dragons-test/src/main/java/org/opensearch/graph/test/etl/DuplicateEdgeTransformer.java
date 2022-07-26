package org.opensearch.graph.test.etl;



import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.test.scenario.ETLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 6/5/2017.
 */
public class DuplicateEdgeTransformer implements Transformer {

    private String id1Field;
    private String id2Field;

    public DuplicateEdgeTransformer(String id1Field, String id2Field) {
        this.id1Field = id1Field;
        this.id2Field = id2Field;
    }
    
    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> docs = new ArrayList<>();
        for (Map<String, String> document : documents) {
            Map<String, String> newDoc = new HashMap<>(document);
            newDoc.put(ETLUtils.DIRECTION_FIELD, Direction.out.toString());
            docs.add(newDoc);

            newDoc = new HashMap<>(document);
            newDoc.put(id1Field, document.get(id2Field));
            newDoc.put(id2Field, document.get(id1Field));
            newDoc.put(ETLUtils.DIRECTION_FIELD, Direction.in.toString());
            docs.add(newDoc);
        }

        return docs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        return builder.addColumn(ETLUtils.DIRECTION_FIELD).build();

    }
}
