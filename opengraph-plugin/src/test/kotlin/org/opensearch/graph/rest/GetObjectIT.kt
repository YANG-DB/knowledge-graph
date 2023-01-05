/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.rest

import org.junit.Assert
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.graph.*
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.BASE_KGRAPH_URI
import org.opensearch.graph.model.RestTag.OBJECT_LIST_FIELD
import java.time.Instant

class GetObjectIT : PluginRestTestCase() {
    private fun createObject(createRequest: String): String {
        val createResponse = executeRequest(
            RestRequest.Method.POST.name,
            "$BASE_KGRAPH_URI/object",
            createRequest,
            RestStatus.OK.status
        )
        val id = createResponse.get("objectId").asString
        Assert.assertNotNull("Id should be generated", id)
        Thread.sleep(100)
        return id
    }

    fun `test get invalid ids`() {
        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object/invalid-id",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(getResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val getIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?objectIdList=invalid-id1,invalid-id2",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(getIdsResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)
    }

    fun `test get single object`() {
        val createRequest = constructQueryTypeRequest()
        val id = createObject(createRequest)

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object/$id",
            "",
            RestStatus.OK.status
        )
        val objectDetails = getResponse.get(OBJECT_LIST_FIELD).asJsonArray.get(0).asJsonObject
        Assert.assertEquals(id, objectDetails.get("objectId").asString)
        Assert.assertEquals(
            jsonify(createRequest).get("schemaEntityType").asJsonObject,
            objectDetails.get("schemaEntityType").asJsonObject
        )
        Thread.sleep(100)
    }

    fun `test get multiple objects`() {
        val emptyResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(0, emptyResponse.get("totalHits").asInt)

        val startTime = Instant.now().toEpochMilli()
        val indexProviderIds = Array(6) { createObject(constructQueryTypeRequest("indexProvider-$it")) }
        val schemaEntityTypeIds =
            Array(3) { createObject(constructQueryTypeRequest("schemaEntityType-$it")) }
        val endTime = Instant.now().toEpochMilli()
        Thread.sleep(1000)

        val getAllResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?maxItems=1000",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(9, getAllResponse.get("totalHits").asInt)

        val getResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?objectType=schemaEntityType",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(3, getResponse.get("totalHits").asInt)
        val list = getResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            schemaEntityTypeIds,
            list.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        val getMultipleTypesResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?objectType=indexProvider,schemaEntityType",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(9, getMultipleTypesResponse.get("totalHits").asInt)
        var multipleTypesList = getMultipleTypesResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds.plus(schemaEntityTypeIds),
            multipleTypesList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        var getMultipleIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?objectIdList=${indexProviderIds.joinToString(",")}",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(6, getMultipleIdsResponse.get("totalHits").asInt)
        val multipleIdsList = getMultipleIdsResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds,
            multipleIdsList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )

        val getIndexProviderIdsResponse = executeRequest(
            RestRequest.Method.GET.name,
            "$BASE_KGRAPH_URI/object?objectType=indexProvider",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(6, getIndexProviderIdsResponse.get("totalHits").asInt)
        multipleTypesList = getIndexProviderIdsResponse.get(OBJECT_LIST_FIELD).asJsonArray
        Assert.assertArrayEquals(
            indexProviderIds,
            multipleTypesList.map { it.asJsonObject.get("objectId").asString }.toTypedArray()
        )
    }
}
