package org.opensearch.graph.executor.cursor.discrete;

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





import org.opensearch.graph.dispatcher.cursor.Cursor;
import org.opensearch.graph.dispatcher.cursor.CursorFactory;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.executor.ontology.schema.PartitionResolver;
import org.opensearch.graph.executor.ontology.schema.ProjectionTransformer;
import org.opensearch.graph.executor.ontology.schema.load.*;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.projection.ProjectionAssignment;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.AssignmentsProjectionResult;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.LoadResponse;
import org.opensearch.graph.model.results.LoadResponse.LoadResponseImpl;
import javaslang.Tuple2;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.client.Client;

import java.util.Collections;
import java.util.List;

import static org.opensearch.graph.model.GlobalConstants.ProjectionConfigs.PROJECTION;
import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;
import static org.opensearch.graph.model.results.LoadResponse.buildAssignment;

public class IndexProjectionCursor extends PathsTraversalCursor {

    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new IndexProjectionCursor((TraversalCursorContext) context);
        }
        //endregion
    }

    /**
     * This projection mapping is a single unified index containing the entire ontology wrapped into a single index so that
     * every type of query result can be indexed and queried for slice & dice type of questions
     * <p>
     * "properties": {
     *   "entityA": {
     *     "type": "nested",
     *     "properties": {
     *       "entityA_id": {
     *         "type": "integer",
     *       },
     *       "relationA": {
     *         "type": "nested",
     *         "properties": {
     *           "relationA_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   },
     *   "entityB": {
     *     "type": "nested",
     *     "properties": {
     *       "entityB_id": {
     *         "type": "integer",
     *       },
     *       "relationB": {
     *         "type": "nested",
     *         "properties": {
     *           "relationB_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   }
     *   }
     *
     */
    public IndexProjectionCursor(TraversalCursorContext context) {
        super(context);
        resolver = new PartitionResolver.StaticPartitionResolver(PROJECTION);
        transformer = new ProjectionTransformer(new Ontology.Accessor(context.getOntology()));
    }

    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        final Query pattern = context.getQueryResource().getQuery();
        builder.withPattern(pattern)
                .withQueryId(context.getQueryResource().getQueryMetadata().getId())
                .withCursorId(context.getQueryResource().getCurrentCursorId())
                .withTimestamp(context.getQueryResource().getQueryMetadata().getCreationTime());

        boolean empty = false;
        do {
            //todo run this via async thread pool
            AssignmentsQueryResult results = super.getNextResults(numResults);
            empty = results.getAssignments().isEmpty();
        } while (!empty);

        return builder.build();
    }

    protected AssignmentsQueryResult toQuery(int numResults) {
        //since a projection index exists - we need to transform an assignment to a document with the projection mapping
        AssignmentsQueryResult result = super.toQuery(numResults);
        //transform assignments results to projection document
        AssignmentsProjectionResult projectionResult = new AssignmentsProjectionResult();
        projectionResult.setPattern(result.getPattern());
        projectionResult.setQueryId(result.getQueryId());
        projectionResult.setCursorId(result.getCursorId());
        projectionResult.setTimestamp(result.getTimestamp());

        if(result.getAssignments().isEmpty()) {
            projectionResult.setAssignments(Collections.emptyList());
            return projectionResult;
        }

        DataTransformerContext<List<ProjectionAssignment>> context = transformer.transform(result, GraphDataLoader.Directive.INSERT);
        LoadResponse<String, GraphError> load = load(this.context.getClient(), context);
        //report back the projection results
        projectionResult.setAssignments(Collections.singletonList(buildAssignment(load)));
        return projectionResult;
    }

    /**
     * load data into E/S
     *
     * @param context
     * @return
     */
    private LoadResponse<String, GraphError> load(Client client, DataTransformerContext context) {
        //load bulk requests
        Tuple2<Response, BulkRequestBuilder> tuple = LoadUtils.load(resolver, client, context);
        //submit bulk request
        LoadUtils.submit(tuple._2(), tuple._1());
        return new LoadResponseImpl().response(context.getTransformationResponse()).response(tuple._1());
    }

    // private region
    private PartitionResolver.StaticPartitionResolver resolver;
    private ProjectionTransformer transformer;


}
