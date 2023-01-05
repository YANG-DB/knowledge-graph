/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.BeforeClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.graph.createObjectFromJsonString
import org.opensearch.graph.getJsonString
import java.io.IOException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RunQueryRequestTests {
    private fun assertGetRequestEquals(
            expected: RunQueryRequest,
            actual: RunQueryRequest
    ) {
        assertEquals(expected.queryId, actual.queryId)
        assertEquals(expected.maxItems, actual.maxItems)
        assertEquals(expected.queryParams, actual.queryParams)
    }

    @Test
    fun `Get request serialize and deserialize transport object should be equal`() {
        val objectRequest = RunQueryRequest(
                "test-id",
                10000,
                mapOf(Pair("filterKey", "filterValue"))
        )
        val recreatedObject = recreateObject(objectRequest) { RunQueryRequest(it) }
        assertGetRequestEquals(objectRequest, recreatedObject)
    }

    @Test
    fun `Get request serialize and deserialize using json object should be equal`() {
        val objectRequest = RunQueryRequest(
                "test-id",
                10000,
                mapOf(Pair("filterKey", "filterValue"))
        )
        val jsonString = getJsonString(objectRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { RunQueryRequest.parse(it) }
        assertGetRequestEquals(objectRequest, recreatedObject)
    }

    @Test
    fun `Get request without query id should throw parse error`() {
        val objectRequest = RunQueryRequest(
                "test-id",
                100,
                mapOf(
                        Pair("filterKey1", "filterValue1"),
                        Pair("filterKey2", "true"),
                        Pair("filterKey3", "filter,Value,3"),
                        Pair("filterKey4", "4")
                )
        )
        val jsonString = """
        {
            "objectId":"test-id",
            "maxItems":"100",
            "queryParamList": {
                "filterKey1":"filterValue1",
                "filterKey2":"true",
                "filterKey3":"filter,Value,3",
                "filterKey4":"4"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { RunQueryRequest.parse(it) }
        assertGetRequestEquals(objectRequest, recreatedObject)
    }


    @Test
    fun `Get request with only filterParamList field should deserialize json object using parser`() {
        val jsonString = """
        {
            "maxItems":"100",
            "filterParamList": {
                "filterKey1":"filterValue1",
                "filterKey2":"true",
                "filterKey3":"filter,Value,3",
                "filterKey4":"4"
            }
        }
        """.trimIndent()
        assertThrows<IOException> {
            createObjectFromJsonString(jsonString) { RunQueryRequest.parse(it) }
        }
    }


    @Test
    fun `Get request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { RunQueryRequest.parse(it) }
        }
    }

    @Test
    fun `Get request should safely ignore extra field in json object`() {
        val objectRequest = RunQueryRequest("test-id")
        val jsonString = """
        {
            "objectId":"test-id",
            "foo":"bar",
            "maxItems":"10000"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { RunQueryRequest.parse(it) }
        assertGetRequestEquals(objectRequest, recreatedObject)
    }
}
