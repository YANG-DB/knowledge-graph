/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.rest

import org.junit.Assert
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.BASE_KGRAPH_URI
import org.opensearch.graph.PluginRestTestCase
import org.opensearch.graph.constructQueryTypeRequest
import org.opensearch.graph.validateErrorResponse
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus

class DeleteObjectIT : PluginRestTestCase() {
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

    fun `test delete invalid ids`() {
        val invalidDeleteIdResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_KGRAPH_URI/object/unknown",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(invalidDeleteIdResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)

        val invalidDeleteIdsResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_KGRAPH_URI/object?objectIdList=does-not-exist1,does-not-exist2",
            "",
            RestStatus.NOT_FOUND.status
        )
        validateErrorResponse(invalidDeleteIdsResponse, RestStatus.NOT_FOUND.status)
        Thread.sleep(100)
    }

    fun `test delete single object`() {
        val createRequest = constructQueryTypeRequest()
        val id = createObject(createRequest)
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_KGRAPH_URI/object/$id",
            "",
            RestStatus.OK.status
        )
        Assert.assertEquals(
            "OK",
            deleteResponse.get("deleteResponseList").asJsonObject.get(id).asString
        )
        Thread.sleep(100)
    }

    fun `test delete multiple objects`() {
        val ids = Array(6) { createObject(constructQueryTypeRequest("indexProvider-$it")) }
        Thread.sleep(1000)
        val deleteResponse = executeRequest(
            RestRequest.Method.DELETE.name,
            "$BASE_KGRAPH_URI/object?objectIdList=${ids.joinToString(separator = ",")}",
            "",
            RestStatus.OK.status
        )
        val deletedObject = deleteResponse.get("deleteResponseList").asJsonObject
        ids.forEach {
            Assert.assertEquals("OK", deletedObject.get(it).asString)
        }
        Thread.sleep(100)
    }
}
