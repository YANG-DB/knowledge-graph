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
import org.opensearch.graph.executor.ontology.schema.PartitionResolver;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.Tuple2;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

public class LoadUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoadUtils.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Tuple2<Response, BulkRequestBuilder> load(PartitionResolver schema, Client client, DataTransformerContext<Object> context) {
        Response upload = new Response("upload");
        BulkRequestBuilder bulk = client.prepareBulk();
        //populate bulk entities documents index requests
        for (DocumentBuilder documentBuilder : context.getEntities()) {
            try {
                if (documentBuilder.isSuccess())
                    buildIndexRequest(schema, client, bulk, documentBuilder);
            } catch (GraphError.GraphErrorException e) {
                upload.failure(e.getError());
            }
        }
        //populate bulk relations document index requests
        for (DocumentBuilder e : context.getRelations()) {
            try {
                if (e.isSuccess())
                    buildIndexRequest(schema, client, bulk, e);
            } catch (GraphError.GraphErrorException err) {
                upload.failure(err.getError());
            }
        }
        return new Tuple2<>(upload, bulk);
    }

    public static IndexRequestBuilder buildIndexRequest(PartitionResolver schema, Client client, BulkRequestBuilder bulk, DocumentBuilder node) {
        try {
            String index = resolveIndex(schema, node);
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
    public static String resolveIndex(PartitionResolver schema, DocumentBuilder node) throws ParseException {
        String nodeType = node.getType();
        Optional<Tuple2<String, String>> field = node.getPartitionField();
        IndexPartitions partitions = schema.getPartition(nodeType);
        //todo validate the partitioned field is indeed the correct time field
        if ((partitions instanceof TimeSeriesIndexPartitions) && field.isPresent()) {
            String indexName = ((TimeSeriesIndexPartitions) partitions).getIndexName(sdf.parse(field.get()._2));
            if (indexName != null) return indexName;
        }
        return partitions.getPartitions().iterator().next().getIndices().iterator().next();
    }

    /**
     * submit bulk request
     *
     * @param bulk
     * @param upload
     * @return
     */
    public static void submit(BulkRequestBuilder bulk, Response upload) {
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
                    //todo - get additional information from request
                    GraphError commitFailed = new GraphError("commit failed", failure.toString());
                    upload.failure(commitFailed);
                    logger.error(commitFailed.toString());
                }
            }
        } catch (Exception err) {
            upload.failure(new GraphError("commit failed", err.toString()));
        }
    }

}
