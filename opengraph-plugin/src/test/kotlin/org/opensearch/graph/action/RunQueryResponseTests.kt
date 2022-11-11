/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject
import org.opensearch.graph.constructQueryObjectDoc
import org.opensearch.graph.createObjectFromJsonString
import org.opensearch.graph.getJsonString
import org.opensearch.graph.model.ObjectSearchResult
import java.time.Instant

internal class RunQueryResponseTests {

    private fun assertSearchResultEquals(
            expected: ObjectSearchResult,
            actual: ObjectSearchResult
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleSampleSchemaObjectDoc = constructQueryObjectDoc()
        val searchResult = ObjectSearchResult(sampleSampleSchemaObjectDoc)
        val searchResponse = RunQueryResponse(searchResult, false)
        val recreatedObject = recreateObject(searchResponse) { RunQueryResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val objectInfo1 = constructQueryObjectDoc("test 1", "test-id-1")
        val objectInfo2 = constructQueryObjectDoc("test 2", "test-id-2")
        val searchResult = ObjectSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(objectInfo1, objectInfo2)
        )
        val searchResponse = RunQueryResponse(searchResult, false)
        val recreatedObject = recreateObject(searchResponse) { RunQueryResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json object object should be equal`() {
        val objectInfo = constructQueryObjectDoc()
        val searchResult = ObjectSearchResult(objectInfo)
        val searchResponse = RunQueryResponse(searchResult, false)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { RunQueryResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple object object should be equal`() {
        val objectInfo1 = constructQueryObjectDoc("test 1", "test-id-1")
        val objectInfo2 = constructQueryObjectDoc("test 2", "test-id-2")
        val searchResult = ObjectSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(objectInfo1, objectInfo2)
        )
        val searchResponse = RunQueryResponse(searchResult, false)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { RunQueryResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }


    @Test
    fun `Search result should throw exception if notificationConfigs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHist":"1",
            "totalHitRelation":"eq",
            "objectList":[
                {
                    "objectId":"object-Id",
                    "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "createdTimeMs":"${createdTimeMs.toEpochMilli()}"
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { RunQueryResponse.parse(it) }
        }
    }
}
