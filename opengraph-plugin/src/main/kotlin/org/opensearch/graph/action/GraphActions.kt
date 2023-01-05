/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.graph.index.KGraphIndex
import org.opensearch.graph.model.ObjectDoc
import org.opensearch.graph.model.ObjectSearchResult
import org.opensearch.graph.security.UserAccessManager
import org.opensearch.graph.util.logger
import org.opensearch.rest.RestStatus
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.LOG_PREFIX
import java.time.Instant

/**
 * Object index operation actions.
 */
internal object GraphActions {
    private val log by logger(GraphActions::class.java)

    /**
     * Create new Object
     * @param request [CreateQueryRequest] object
     * @return [CreateQueryResponse]
     */
    fun create(request: CreateQueryRequest, user: User?): CreateQueryResponse {
        log.info("$LOG_PREFIX:KGraphObject-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val objectDoc = ObjectDoc(
                "ignore",
                currentTime,
                currentTime,
                UserAccessManager.getUserTenant(user),
                UserAccessManager.getAllAccessInfo(user),
                request.type,
                request.objectData
        )
        val docId = KGraphIndex.createObject(objectDoc, request.objectId)
        docId ?: throw OpenSearchStatusException(
                "Object Creation failed",
                RestStatus.INTERNAL_SERVER_ERROR
        )
        return CreateQueryResponse(docId)
    }

    /**
     * Get Object info
     * @param request [RunQueryRequest] object
     * @return [RunQueryResponse]
     */
    fun run(request: RunQueryRequest, user: User?): RunQueryResponse {
        log.info("$LOG_PREFIX:KGraphObject-run ${request.queryId}")
        UserAccessManager.validateUser(user)
        return run(request.queryId, user)
    }

    /**
     * Get Object info
     * @param objectId object id
     * @param user the user info object
     * @return [RunQueryResponse]
     */
    private fun run(objectId: String, user: User?): RunQueryResponse {
        val objectDocInfo = KGraphIndex.getObject(objectId)
        objectDocInfo
                ?: run {
                    throw OpenSearchStatusException("Object $objectId not found", RestStatus.NOT_FOUND)
                }
        val currentDoc = objectDocInfo.objectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException("Permission denied for Object $objectId", RestStatus.FORBIDDEN)
        }
        val docInfo = ObjectDoc(
                objectId,
                currentDoc.updatedTime,
                currentDoc.createdTime,
                currentDoc.tenant,
                currentDoc.access,
                currentDoc.type,
                currentDoc.objectData
        )
        // TODO execute the query and return call back here as part of the response

        return RunQueryResponse(
                ObjectSearchResult(docInfo),
                UserAccessManager.hasAllInfoAccess(user)
        )
    }

    /**
     * Get Object info
     * @param objectId object id
     * @param user the user info object
     * @return [QueryInfoObjectResponse]
     */
    private fun info(objectId: String, user: User?): QueryInfoObjectResponse {
        log.info("$LOG_PREFIX:KGraphObject-info $objectId")
        val objectDocInfo = KGraphIndex.getObject(objectId)
        objectDocInfo
                ?: run {
                    throw OpenSearchStatusException("Object $objectId not found", RestStatus.NOT_FOUND)
                }
        val currentDoc = objectDocInfo.objectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException("Permission denied for Object $objectId", RestStatus.FORBIDDEN)
        }
        val docInfo = ObjectDoc(
                objectId,
                currentDoc.updatedTime,
                currentDoc.createdTime,
                currentDoc.tenant,
                currentDoc.access,
                currentDoc.type,
                currentDoc.objectData
        )
        return QueryInfoObjectResponse(
                ObjectSearchResult(docInfo),
                UserAccessManager.hasAllInfoAccess(user)
        )
    }

    /**
     * Get Object info
     * @param objectIds object id set
     * @param user the user info object
     * @return [RunQueryResponse]
     */
    private fun info(objectIds: Set<String>, user: User?): QueryInfoObjectResponse {
        val objectDocs = KGraphIndex.getObjects(objectIds)
        if (objectDocs.size != objectIds.size) {
            val mutableSet = objectIds.toMutableSet()
            objectDocs.forEach { mutableSet.remove(it.id) }
            throw OpenSearchStatusException(
                    "Object $mutableSet not found",
                    RestStatus.NOT_FOUND
            )
        }
        objectDocs.forEach {
            val currentDoc = it.objectDoc
            if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
                throw OpenSearchStatusException(
                        "Permission denied for Object ${it.id}",
                        RestStatus.FORBIDDEN
                )
            }
        }
        val configSearchResult = objectDocs.map {
            ObjectDoc(
                    it.id!!,
                    it.objectDoc.updatedTime,
                    it.objectDoc.createdTime,
                    it.objectDoc.tenant,
                    it.objectDoc.access,
                    it.objectDoc.type,
                    it.objectDoc.objectData
            )
        }
        return QueryInfoObjectResponse(
                ObjectSearchResult(configSearchResult),
                UserAccessManager.hasAllInfoAccess(user)
        )
    }


    fun get(request: QueryInfoObjectRequest, user: User?): QueryInfoObjectResponse {
        log.info("$LOG_PREFIX:KGraphObject-get ${request.objectIds}")
        UserAccessManager.validateUser(user)
        return when (request.objectIds.size) {
            0 -> getAll(request, user)
            1 -> info(request.objectIds.first(), user)
            else -> info(request.objectIds, user)
        }
    }

    /**
     * Get all Object matching the criteria
     * @param request [RunQueryRequest] object
     * @param user the user info object
     * @return [RunQueryResponse]
     */
    private fun getAll(request: QueryInfoObjectRequest, user: User?): QueryInfoObjectResponse {
        log.info("$LOG_PREFIX:KGraphObject-getAll")
        val searchResult = KGraphIndex.getAllObjects(
                UserAccessManager.getUserTenant(user),
                UserAccessManager.getSearchAccessInfo(user),
                request
        )
        return QueryInfoObjectResponse(searchResult, UserAccessManager.hasAllInfoAccess(user))
    }

    /**
     * Delete Object
     * @param request [GenericObjectRequest] object
     * @param user the user info object
     * @return [DeleteQueryResponse]
     */
    fun delete(request: GenericObjectRequest, user: User?): DeleteQueryResponse {
        log.info("$LOG_PREFIX:KGraphObject-delete ${request.objectIds}")
        return if (request.objectIds.size == 1) {
            delete(request.objectIds.first(), user)
        } else {
            delete(request.objectIds, user)
        }
    }

    /**
     * Delete by object id
     *
     * @param objectId
     * @param user
     * @return [DeleteQueryResponse]
     */
    private fun delete(objectId: String, user: User?): DeleteQueryResponse {
        log.info("$LOG_PREFIX:KGraphObject-delete $objectId")
        UserAccessManager.validateUser(user)
        val objectDocInfo = KGraphIndex.getObject(objectId)
        objectDocInfo
                ?: run {
                    throw OpenSearchStatusException(
                            "KGraphObject $objectId not found",
                            RestStatus.NOT_FOUND
                    )
                }

        val currentDoc = objectDocInfo.objectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException(
                    "Permission denied for Object $objectId",
                    RestStatus.FORBIDDEN
            )
        }
        if (!KGraphIndex.deleteObject(objectId)) {
            throw OpenSearchStatusException(
                    "Object $objectId delete failed",
                    RestStatus.REQUEST_TIMEOUT
            )
        }
        return DeleteQueryResponse(mapOf(Pair(objectId, RestStatus.OK)))
    }

    /**
     * Delete Object
     * @param objectIds Object object ids
     * @param user the user info object
     * @return [DeleteQueryResponse]
     */
    fun delete(objectIds: Set<String>, user: User?): DeleteQueryResponse {
        log.info("$LOG_PREFIX:KGraphObject-delete $objectIds")
        UserAccessManager.validateUser(user)
        val configDocs = KGraphIndex.getObjects(objectIds)
        if (configDocs.size != objectIds.size) {
            val mutableSet = objectIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.id) }
            throw OpenSearchStatusException(
                    "Object $mutableSet not found",
                    RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            val currentDoc = it.objectDoc
            if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
                throw OpenSearchStatusException(
                        "Permission denied for Object ${it.id}",
                        RestStatus.FORBIDDEN
                )
            }
        }
        val deleteStatus = KGraphIndex.deleteObjects(objectIds)
        return DeleteQueryResponse(deleteStatus)
    }
}
