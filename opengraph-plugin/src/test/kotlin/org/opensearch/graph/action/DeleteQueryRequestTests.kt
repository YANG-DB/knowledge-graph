/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.graph.createObjectFromJsonString
import org.opensearch.graph.getJsonString

internal class DeleteQueryRequestTests {
    @Test
    fun `Delete request serialize and deserialize transport object should be equal`() {
        val deleteRequest = GenericObjectRequest(setOf("test-id"))
        val recreatedObject = recreateObject(deleteRequest) { GenericObjectRequest(it) }
        assertEquals(deleteRequest.objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request serialize and deserialize using json object should be equal`() {
        val deleteRequest = GenericObjectRequest(setOf("sample_config_id"))
        val jsonString = getJsonString(deleteRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { GenericObjectRequest.parse(it) }
        assertEquals(deleteRequest.objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request should deserialize json object using parser`() {
        val objectId = "test-id"
        val objectIds = setOf(objectId)
        val jsonString = """
        {
            "objectIdList":["$objectId"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GenericObjectRequest.parse(it) }
        assertEquals(objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { GenericObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Delete request should throw exception when objectIdLists is replace with objectIdLists2 in json object`() {
        val jsonString = """
        {
            "objectIdLists":["test-id"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { GenericObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Delete request should safely ignore extra field in json object`() {
        val objectId = "test-id"
        val objectIds = setOf(objectId)
        val jsonString = """
        {
            "objectIdList":["$objectId"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GenericObjectRequest.parse(it) }
        assertEquals(objectIds, recreatedObject.objectIds)
    }
}
