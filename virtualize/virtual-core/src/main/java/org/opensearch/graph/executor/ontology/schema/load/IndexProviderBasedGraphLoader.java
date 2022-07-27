package org.opensearch.graph.executor.ontology.schema.load;

/*-
 * #%L
 * virtual-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.Tuple2;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.common.xcontent.XContentType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.opensearch.graph.executor.ontology.schema.load.DataLoaderUtils.extractFile;
import static org.opensearch.graph.model.results.LoadResponse.LoadResponseImpl;

public class IndexProviderBasedGraphLoader implements GraphDataLoader<String, GraphError> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    public static final int NUM_IDS = 1000;
    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Client client;
    private EntityTransformer transformer;
    private RawSchema schema;
    private ObjectMapper mapper;
    private IdGeneratorDriver<Range> idGenerator;


    @Inject
    public IndexProviderBasedGraphLoader(Client client, EntityTransformer transformer, RawSchema schema, IdGeneratorDriver<Range> idGenerator) {
        this.client = client;
        this.schema = schema;
        this.transformer = transformer;
        this.idGenerator = idGenerator;
        this.mapper = new ObjectMapper();

    }

    @Override
    public LoadResponse<String, GraphError> load(String ontology, LogicalGraphModel root, Directive directive) {
        //todo load correct ontology graph transformer and use it to transform data to the actual schema structure
        BulkRequestBuilder bulk = client.prepareBulk();
        Response upload = new Response("Upload");
        DataTransformerContext<LogicalGraphModel> context = transformer.transform(root, directive);
        //load bulk requests
        load(bulk, upload, context);
        //submit bulk request
        submit(bulk, upload);

        return new LoadResponseImpl().response(context.getTransformationResponse()).response(upload);
    }

    private void submit(BulkRequestBuilder bulk, Response upload) {
        //bulk index data
        try {
            BulkResponse responses = bulk.get();
            final BulkItemResponse[] items = responses.getItems();
            for (BulkItemResponse item : items) {
                if (!item.isFailed()) {
                    upload.success(item.getId());
                } else {
                    //log error
                    BulkItemResponse.Failure failure = item.getFailure();
                    DocWriteRequest<?> request = bulk.request().requests().get(item.getItemId());
                    //todo - get TechId from request
                    upload.failure(new GraphError("commit failed", failure.toString()));
                }

            }
        }catch (Exception err) {
            upload.failure(new GraphError("commit failed", err.toString()));
        }
    }

    private void load(BulkRequestBuilder bulk, Response upload, DataTransformerContext<LogicalGraphModel> context) {
        //populate bulk entities documents index requests
        for (DocumentBuilder documentBuilder : context.getEntities()) {
            try {
                buildIndexRequest(bulk, documentBuilder);
            } catch (GraphError.GraphErrorException e) {
                upload.failure(e.getError());
            }
        }
        //populate bulk relations document index requests
        for (DocumentBuilder e : context.getRelations()) {
            try {
                buildIndexRequest(bulk, e);
            } catch (GraphError.GraphErrorException err) {
                upload.failure(err.getError());
            }
        }
    }

    public IndexRequestBuilder buildIndexRequest(BulkRequestBuilder bulk, DocumentBuilder node) {
        try {
            String index = resolveIndex(node);
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(index.toLowerCase())
                    .setType(node.getType())
                    .setId(node.getId())
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(mapper.writeValueAsString(node.getNode()), XContentType.JSON);
            node.getRouting().ifPresent(request::setRouting);
            bulk.add(request);
            return request;
        } catch (Throwable err) {
            throw new GraphError.GraphErrorException("Error while building Index request", err,
                    new GraphError("Error while building Index request", err.getMessage()));
        }
    }

    /**
     * resolve index name according to schema and in case of range partitioned index - according to the partitioning field value
     *
     * @param node
     * @return
     */
    private String resolveIndex(DocumentBuilder node) throws ParseException {
        String nodeType = node.getType();
        Optional<Tuple2<String, String>> field = node.getPartitionField();
        IndexPartitions partitions = schema.getPartition(nodeType);
        //todo validate the partitioned field is indeed the correct time field
        if ((partitions instanceof TimeSeriesIndexPartitions) && field.isPresent()) {
            String indexName = ((TimeSeriesIndexPartitions) partitions).getIndexName(sdf.parse(field.get()._2));
            if (indexName != null) return indexName;
        }
        //get the first matching index to populate
        return partitions.getIndices().iterator().next();
    }


    @Override
    public LoadResponse<String, GraphError> load(String ontology, File data, Directive directive) throws IOException {
        String contentType = Files.probeContentType(data.toPath());
        if(Objects.isNull(contentType))
            contentType = DataLoaderUtils.getZipType(data);
        if (Arrays.asList("application/gzip", "application/zip").contains(contentType)) {
            List<File> files = Collections.EMPTY_LIST;
            switch (contentType) {
                case "application/gzip":
                    files = DataLoaderUtils.extractFile(data);
                    break;
                case "application/zip":
                    files = DataLoaderUtils.extractFile(data);
                    break;
            }
            if(!files.isEmpty()) {
                //currently assuming a single entry zip fle
                data = files.get(0);
            }
        }
        String graph = new String(Files.readAllBytes(data.toPath()));
        //read
        LogicalGraphModel root = mapper.readValue(graph, LogicalGraphModel.class);
        return load(ontology, root, directive);
    }


    public Range.StatefulRange getRange(String type) {
        //init ranges
        Range.StatefulRange statefulRange = ranges.computeIfAbsent(type,
                s -> new Range.StatefulRange(idGenerator.getNext(type, NUM_IDS)));

        if (statefulRange.hasNext())
            return statefulRange;
        //update ranges
        ranges.put(type, new Range.StatefulRange(idGenerator.getNext(type, NUM_IDS)));
        //return next range
        return ranges.get(type);
    }

}
