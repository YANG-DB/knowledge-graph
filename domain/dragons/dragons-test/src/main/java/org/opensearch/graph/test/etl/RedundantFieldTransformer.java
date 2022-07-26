package org.opensearch.graph.test.etl;



import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.test.scenario.ETLUtils;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 6/5/2017.
 */
public class RedundantFieldTransformer implements Transformer{
    private TransportClient client;
    private Map<String, String> entityADupFields;
    private String entityADupIdField;
    private List<String> entityAIndices;
    private String entityAType;
    private Map<String, String> entityBDupFields;
    private String entityBDupIdField;
    private List<String> entityBIndices;
    private String entityBType;
    private String direction;

    public RedundantFieldTransformer(TransportClient client,
                                     Map<String, String> entityADupFields,
                                     String entityADupIdField,
                                     List<String> entityAIndices,
                                     String entityAType,
                                     Map<String, String> entityBDupFields,
                                     String entityBDupIdField,
                                     List<String> entityBIndices,
                                     String entityBType,
                                     String direction) {
        this.client = client;
        this.entityADupFields = entityADupFields;
        this.entityADupIdField = entityADupIdField;
        this.entityAIndices = entityAIndices;
        this.entityAType = entityAType;
        this.direction = direction;
        this.entityBDupFields = entityBDupFields;
        this.entityBDupIdField = entityBDupIdField;
        this.entityBIndices = entityBIndices;
        this.entityBType = entityBType;
    }
    public RedundantFieldTransformer(TransportClient client, Map<String, String> entityADupFields, String entityADupIdField, List<String> entityAIndices, String entityAType,
                                     Map<String, String> entityBDupFields,
                                     String entityBDupIdField,
                                     List<String> entityBIndices,
                                     String entityBType) {
        this(client, entityADupFields, entityADupIdField, entityAIndices, entityAType,entityBDupFields,entityBDupIdField,entityBIndices,entityBType, Direction.both.name());
    }
    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> documentsOld = documents;
        if(!direction.equals(Direction.both.name()))
            documents = documents.stream().filter(doc -> doc.get(ETLUtils.DIRECTION_FIELD).equals(direction)).collect(Collectors.toList());
        List<Map<String, String>> newDocuments = addRedundantFields(documents, entityAType, entityADupIdField, entityAIndices, entityADupFields);
        newDocuments = addRedundantFields(newDocuments, entityBType, entityBDupIdField, entityBIndices, entityBDupFields);
        if(!direction.equals(Direction.both.name()))
            newDocuments.addAll(documentsOld.stream().filter(doc -> !doc.get(ETLUtils.DIRECTION_FIELD).equals(direction)).collect(Collectors.toList()));
        return newDocuments;
    }

    private List<Map<String, String>> addRedundantFields(List<Map<String, String>> documents, String entityType,String entityDupIdField,List<String> indices, Map<String, String> entityDupFields) {
        List<String> idValues = new ArrayList<>();
        documents.forEach(doc -> idValues.add(ETLUtils.id(entityType,doc.get(entityDupIdField))));
        String[] arr = new String[indices.size()];
        indices.toArray(arr);
        String[]ids = new String[idValues.size()];
        idValues.toArray(ids);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(arr).setTypes(entityType).
                                                                            setQuery(QueryBuilders.idsQuery(entityType).addIds(ids)).
                                                                            setSize(ids.length);
        searchRequestBuilder.setFetchSource(true);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        Map<String, Map<String, String>> dupsMap = new HashMap<>();

        for (SearchHit hit : searchResponse.getHits()) {
            Map<String,String> fields = new HashMap<>();
            entityDupFields.keySet().forEach(f -> {
                try {
                    fields.put(f, hit.getFields().get(f).getValue().toString());
                }catch(Exception ex){
                    throw ex;
                }});
            dupsMap.put(hit.getId(), fields);
        }
        List<Map<String, String>> newDocuments = new ArrayList<>(documents.size());
        for (Map<String, String> document : documents) {
            Map<String, String> newDocument = new HashMap<>(document);
            Map<String, String> fields = dupsMap.get(ETLUtils.id(entityType,document.get(entityDupIdField)));
            entityDupFields.forEach((k, v) -> {
                try {
                    newDocument.put(v, fields.get(k));
                }catch(Exception ex)
                {
                    throw ex;
                }
            });
            newDocuments.add(newDocument);
        }
        return newDocuments;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        entityADupFields.values().stream().sorted().forEach(v -> builder.addColumn(v));
        entityBDupFields.values().stream().sorted().forEach(v -> builder.addColumn(v));
        return builder.build();
    }
}
