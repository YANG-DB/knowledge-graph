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

class RunQueryIT : PluginRestTestCase() {
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

    fun `test run invalid id`() {
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

    fun `test run single object`() {
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

}
