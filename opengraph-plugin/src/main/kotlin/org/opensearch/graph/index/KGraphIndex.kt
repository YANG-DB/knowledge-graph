/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.index

import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.admin.indices.mapping.put.PutMappingRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.get.MultiGetRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.client.Client
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.index.IndexNotFoundException
import org.opensearch.index.query.QueryBuilders
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.LOG_PREFIX
import org.opensearch.graph.action.QueryInfoObjectRequest
import org.opensearch.graph.model.ObjectDoc
import org.opensearch.graph.model.ObjectDocInfo
import org.opensearch.graph.model.ObjectSearchResult
import org.opensearch.graph.model.RestTag.ACCESS_LIST_FIELD
import org.opensearch.graph.model.RestTag.TENANT_FIELD
import org.opensearch.graph.model.SearchResults
import org.opensearch.graph.settings.PluginSettings
import org.opensearch.graph.util.SecureIndexClient
import org.opensearch.graph.util.logger
import org.opensearch.rest.RestStatus
import org.opensearch.search.SearchHit
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.graph.action.RunQueryRequest
import java.util.concurrent.TimeUnit

/**
 * Class for doing OpenSearch index operation to maintain KGraphObject objects in cluster.
 */
@Suppress("TooManyFunctions")
internal object KGraphIndex {
    private val log by logger(KGraphIndex::class.java)
    private const val INDEX_NAME = ".opensearch-graph"
    private const val KGRAPH_MAPPING_FILE_NAME =  "kgraph-mapping.yml"
    private const val KGRAPH_SETTINGS_FILE_NAME = "kgraph-settings.yml"
    private const val MAPPING_TYPE = "_doc"

    private var mappingsUpdated: Boolean = false
    private lateinit var client: Client
    private lateinit var clusterService: ClusterService

    private val searchHitParser = object : SearchResults.SearchHitParser<ObjectDoc> {
        override fun parse(searchHit: SearchHit): ObjectDoc {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                searchHit.sourceAsString
            )
            parser.nextToken()
            return ObjectDoc.parse(parser, searchHit.id)
        }
    }

    /**
     * Initialize the class
     * @param client The OpenSearch client
     * @param clusterService The OpenSearch cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService) {
        KGraphIndex.client = SecureIndexClient(client)
        KGraphIndex.clusterService = clusterService
        mappingsUpdated = false
    }

    /**
     * Create index using the mapping and settings defined in resource
     */
    @Suppress("TooGenericExceptionCaught")
    private fun createIndex() {
        if (!isIndexExists(INDEX_NAME)) {
            val classLoader = KGraphIndex::class.java.classLoader
            val indexMappingSource = classLoader.getResource(KGRAPH_MAPPING_FILE_NAME)?.readText()!!
            val indexSettingsSource = classLoader.getResource(KGRAPH_SETTINGS_FILE_NAME)?.readText()!!
            val request = CreateIndexRequest(INDEX_NAME)
                .mapping(indexMappingSource, XContentType.YAML)
                .settings(indexSettingsSource, XContentType.YAML)
            try {
                val actionFuture = client.admin().indices().create(request)
                val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                if (response.isAcknowledged) {
                    log.info("$LOG_PREFIX:Index $INDEX_NAME creation Acknowledged")
//                    reindex()
                } else {
                    throw IllegalStateException("$LOG_PREFIX:Index $INDEX_NAME creation not Acknowledged")
                }
            } catch (exception: Exception) {
                if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                    throw exception
                }
            }
            mappingsUpdated = true
        } else if (!mappingsUpdated) {
            updateMappings()
        }
    }

    /**
     * Check if the index mappings have changed and if they have, update them
     */
    private fun updateMappings() {
        val classLoader = KGraphIndex::class.java.classLoader
        val indexMappingSource = classLoader.getResource(KGRAPH_MAPPING_FILE_NAME)?.readText()!!
        val request = PutMappingRequest(INDEX_NAME)
            .source(indexMappingSource, XContentType.YAML)
        try {
            val actionFuture = client.admin().indices().putMapping(request)
            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
            if (response.isAcknowledged) {
                log.info("$LOG_PREFIX:Index $INDEX_NAME update mapping Acknowledged")
            } else {
                throw IllegalStateException("$LOG_PREFIX:Index $INDEX_NAME update mapping not Acknowledged")
            }
            mappingsUpdated = true
        } catch (exception: IndexNotFoundException) {
            log.error("$LOG_PREFIX:IndexNotFoundException:", exception)
        }
    }

    /**
     * Check if the index is created and available.
     * @param index
     * @return true if index is available, false otherwise
     */
    private fun isIndexExists(index: String): Boolean {
        val clusterState = clusterService.state()
        return clusterState.routingTable.hasIndex(index)
    }

    /**
     * Create  object
     *
     * @param objectDoc
     * @param id
     * @return object id if successful, otherwise null
     */
    fun createObject(objectDoc: ObjectDoc, id: String? = null): String? {
        createIndex()
        val xContent = objectDoc.toXContent()
        val indexRequest = IndexRequest(INDEX_NAME)
            .source(xContent)
            .create(true)
        if (id != null) {
            indexRequest.id(id)
        }
        val actionFuture = client.index(indexRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.result != DocWriteResponse.Result.CREATED) {
            log.warn("$LOG_PREFIX:createKGraphObject - response:$response")
            null
        } else {
            response.id
        }
    }

    /**
     * Get  object
     *
     * @param id
     * @return [ObjectDocInfo]
     */
    fun getObject(id: String): ObjectDocInfo? {
        createIndex()
        val getRequest = GetRequest(INDEX_NAME).id(id)
        val actionFuture = client.get(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return parseObjectDoc(id, response)
    }

    /**
     * Get multiple  objects
     *
     * @param ids
     * @return list of [ObjectDocInfo]
     */
    fun getObjects(ids: Set<String>): List<ObjectDocInfo> {
        createIndex()
        val getRequest = MultiGetRequest()
        ids.forEach { getRequest.add(INDEX_NAME, it) }
        val actionFuture = client.multiGet(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return response.responses.mapNotNull { parseObjectDoc(it.id, it.response) }
    }

    /**
     * Parse object doc
     *
     * @param id
     * @param response
     * @return [ObjectDocInfo]
     */
    private fun parseObjectDoc(id: String, response: GetResponse): ObjectDocInfo? {
        return if (response.sourceAsString == null) {
            log.warn("$LOG_PREFIX:getKGraphObject - $id not found; response:$response")
            null
        } else {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString
            )
            parser.nextToken()
            val doc = ObjectDoc.parse(parser, id)
            ObjectDocInfo(id, response.version, response.seqNo, response.primaryTerm, doc)
        }
    }

    /**
     * Get all  objects
     *
     * @param tenant
     * @param access
     * @param request
     * @return [ObjectSearchResult]
     */
    fun getAllObjects(
        tenant: String,
        access: List<String>,
        request: QueryInfoObjectRequest
    ): ObjectSearchResult {
        createIndex()
        val queryHelper = KGraphQueryHelper(request.types)
        val sourceBuilder = SearchSourceBuilder()
            .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
            .from(request.fromIndex)
        queryHelper.addSortField(sourceBuilder, request.sortField, request.sortOrder)

        val query = QueryBuilders.boolQuery()
        query.filter(QueryBuilders.termsQuery(TENANT_FIELD, tenant))
        if (access.isNotEmpty()) {
            query.filter(QueryBuilders.termsQuery(ACCESS_LIST_FIELD, access))
        }
        queryHelper.addTypeFilters(query)
        queryHelper.addQueryFilters(query, request.filterParams)
        sourceBuilder.query(query)
        val searchRequest = SearchRequest()
            .indices(INDEX_NAME)
            .source(sourceBuilder)
        val actionFuture = client.search(searchRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val result = ObjectSearchResult(request.fromIndex.toLong(), response, searchHitParser)
        log.info(
            "$LOG_PREFIX:getAllKGraphObjects types:${request.types} from:${request.fromIndex}," +
                " sortField:${request.sortField}, sortOrder=${request.sortOrder}, filters=${request.filterParams}" +
                " retCount:${result.objectList.size}, totalCount:${result.totalHits}"
        )
        return result
    }

    /**
     * Delete  object
     *
     * @param id
     * @return true if successful, otherwise false
     */
    fun deleteObject(id: String): Boolean {
        createIndex()
        val deleteRequest = DeleteRequest()
            .index(INDEX_NAME)
            .id(id)
        val actionFuture = client.delete(deleteRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.DELETED) {
            log.warn("$LOG_PREFIX:deleteKGraphObjectObject failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.DELETED
    }

    /**
     * Delete multiple  objects
     *
     * @param ids
     * @return map of id to delete status
     */
    fun deleteObjects(ids: Set<String>): Map<String, RestStatus> {
        createIndex()
        val bulkRequest = BulkRequest()
        ids.forEach {
            val deleteRequest = DeleteRequest()
                .index(INDEX_NAME)
                .id(it)
            bulkRequest.add(deleteRequest)
        }
        val actionFuture = client.bulk(bulkRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val mutableMap = mutableMapOf<String, RestStatus>()
        response.forEach {
            mutableMap[it.id] = it.status()
            if (it.isFailed) {
                log.warn("$LOG_PREFIX:deleteKGraphObjects failed for ${it.id}; response:${it.failureMessage}")
            }
        }
        return mutableMap
    }
}
