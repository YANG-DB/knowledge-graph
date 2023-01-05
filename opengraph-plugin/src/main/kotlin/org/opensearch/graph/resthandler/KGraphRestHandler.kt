/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.graph.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.utils.logger
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.BASE_KGRAPH_URI
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.LOG_PREFIX
import org.opensearch.graph.action.*
import org.opensearch.graph.index.KGraphQueryHelper
import org.opensearch.graph.model.ObjectType
import org.opensearch.graph.model.RestTag.FILTER_PARAM_LIST_FIELD
import org.opensearch.graph.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.graph.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_ID_LIST_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_TYPE_FIELD
import org.opensearch.graph.model.RestTag.QUERY_PARAM_LIST_FIELD
import org.opensearch.graph.model.RestTag.SORT_FIELD_FIELD
import org.opensearch.graph.model.RestTag.SORT_ORDER_FIELD
import org.opensearch.graph.settings.PluginSettings
import org.opensearch.graph.util.contentParserNextToken
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.*
import org.opensearch.rest.RestStatus
import org.opensearch.search.sort.SortOrder
import java.util.*

/**
 * Rest handler for KGraphObject lifecycle management.
 * This handler uses [KGraphActions].
 */
internal class KGraphRestHandler : BaseRestHandler() {
    companion object {
        private const val KGRAPH_ACTION = "kgraph_actions"
        private const val KGRAPH_URL = "$BASE_KGRAPH_URI/object"
        private val log by logger(KGraphRestHandler::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return KGRAPH_ACTION
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Create a new object
             * Request URL: POST KGRAPH_URL
             * Request body: Ref [org.opensearch.graph.model.CreateQueryRequest]
             * Response body: Ref [org.opensearch.graph.model.CreateQueryResponse]
             */
            Route(POST, KGRAPH_URL),
            /**
             * Get a object
             * Request URL: GET KGRAPH_URL/{objectId}
             * Request body: Ref [org.opensearch.graph.model.RunQueryRequest]
             * Response body: Ref [org.opensearch.graph.model.RunQueryResponse]
             */
            Route(GET, "$KGRAPH_URL/{$OBJECT_ID_FIELD}"),
            Route(GET, KGRAPH_URL),
            /**
             * Delete object
             * Request URL: DELETE KGRAPH_URL/{objectId}
             * Request body: Ref [org.opensearch.graph.model.DeleteQueryRequest]
             * Response body: Ref [org.opensearch.graph.model.DeleteQueryResponse]
             */
            Route(DELETE, "$KGRAPH_URL/{$OBJECT_ID_FIELD}"),
            Route(DELETE, "$KGRAPH_URL")
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(
            OBJECT_ID_FIELD,
            OBJECT_ID_LIST_FIELD,
            OBJECT_TYPE_FIELD,
            SORT_FIELD_FIELD,
            SORT_ORDER_FIELD,
            FROM_INDEX_FIELD,
            MAX_ITEMS_FIELD
        )
    }

    private fun executePostRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return RestChannelConsumer {
            client.execute(
                CreateQueryAction.ACTION_TYPE,
                CreateQueryRequest.parse(request.contentParserNextToken()),
                RestResponseToXContentListener(it)
            )
        }
    }
    private fun executeInfoRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val objectId: String? = request.param(OBJECT_ID_FIELD)
        val objectIdListString: String? = request.param(OBJECT_ID_LIST_FIELD)
        val objectIdList = getObjectIdSet(objectId, objectIdListString)
        val types: EnumSet<ObjectType> = getTypesSet(request.param(OBJECT_TYPE_FIELD))
        val sortField: String? = request.param(SORT_FIELD_FIELD)
        val sortOrderString: String? = request.param(SORT_ORDER_FIELD)
        val sortOrder: SortOrder? = if (sortOrderString == null) {
            null
        } else {
            SortOrder.fromString(sortOrderString)
        }
        val fromIndex = request.param(FROM_INDEX_FIELD)?.toIntOrNull() ?: 0
        val maxItems = request.param(MAX_ITEMS_FIELD)?.toIntOrNull() ?: PluginSettings.defaultMaxFetchCount
        val filterParams = request.params()
                .filter { KGraphQueryHelper.FILTER_PARAMS.contains(it.key) }
                .map { Pair(it.key, request.param(it.key)) }
                .toMap()
        log.info(
                "$LOG_PREFIX:executeGetRequest idList:$objectIdList types:$types, from:$fromIndex, maxItems:$maxItems," +
                        " sortField:$sortField, sortOrder=$sortOrder, filters=$filterParams"
        )
        return RestChannelConsumer {
            client.execute(
                    QueryInfoAction.ACTION_TYPE,
                    QueryInfoObjectRequest(
                            objectIdList,
                            types,
                            fromIndex,
                            sortField,
                            sortOrder,
                            filterParams
                    ),
                    RestResponseToXContentListener(it)
            )
        }
    }

    private fun executeRunRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val objectId = request.param(OBJECT_ID_FIELD)
        val maxItems = request.param(MAX_ITEMS_FIELD)?.toIntOrNull() ?: PluginSettings.defaultMaxFetchCount
        val queryParams = request.params()
                .filter { it.key.startsWith(QUERY_PARAM_LIST_FIELD) }
                .map { Pair(it.key, request.param(it.key)) }
                .toMap()
        log.info(
            "$LOG_PREFIX:executeGetRequest  objectId:$objectId, maxItems:$maxItems," +
                " queryParams=$queryParams"
        )
        return RestChannelConsumer {
            client.execute(
                RunQueryAction.ACTION_TYPE,
                RunQueryRequest(
                    objectId,
                    maxItems,
                    queryParams
                ),
                RestResponseToXContentListener(it)
            )
        }
    }

    private fun executeDeleteRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val objectId: String? = request.param(OBJECT_ID_FIELD)
        val objectIdSet: Set<String> =
            request.paramAsStringArray(OBJECT_ID_LIST_FIELD, arrayOf(objectId))
                .filter { s -> !s.isNullOrBlank() }
                .toSet()
        return RestChannelConsumer {
            if (objectIdSet.isEmpty()) {
                it.sendResponse(
                    BytesRestResponse(
                        RestStatus.BAD_REQUEST,
                        "either $OBJECT_ID_FIELD or $OBJECT_ID_LIST_FIELD is required"
                    )
                )
            } else {
                client.execute(
                    DeleteQueryAction.ACTION_TYPE,
                    DeleteQueryRequest(objectIdSet),
                    RestResponseToXContentListener(it)
                )
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> executePostRequest(request, client)
            GET -> executeInfoRequest(request, client)
            DELETE -> executeDeleteRequest(request, client)
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }

    private fun getObjectIdSet(objectId: String?, objectIdList: String?): Set<String> {
        var retIds: Set<String> = setOf()
        if (objectId != null) {
            retIds = setOf(objectId)
        }
        if (objectIdList != null) {
            retIds = objectIdList.split(",").union(retIds)
        }
        return retIds
    }

    private fun getTypesSet(typesString: String?): EnumSet<ObjectType> {
        var types: EnumSet<ObjectType> = EnumSet.noneOf(ObjectType::class.java)
        typesString?.split(",")?.forEach { types.add(ObjectType.fromTagOrDefault(it)) }
        return types
    }
}
