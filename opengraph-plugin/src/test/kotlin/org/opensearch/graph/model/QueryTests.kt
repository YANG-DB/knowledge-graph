/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.graph.createObjectFromJsonString
import org.opensearch.graph.getJsonString
import org.opensearch.graph.model.query.Start
import org.opensearch.graph.model.query.entity.ETyped
import org.opensearch.graph.model.query.properties.EProp
import org.opensearch.graph.model.query.properties.EPropGroup
import org.opensearch.graph.model.query.properties.constraint.Constraint
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp
import org.opensearch.graph.model.transport.CreateQueryRequest
import java.util.*

internal class QueryTests {
    var query = org.opensearch.graph.model.query.Query.Builder.instance().withName("q2").withOnt("Dragons")
            .withElements(Arrays.asList(
                    Start(0, 1),
                    ETyped(1, "P", "Person", 2, 0),
                    EPropGroup(2,
                            EProp(3, "name", Constraint.of(ConstraintOp.like, "jhon*")))
            )).build()

    val sample = Query("test", CreateQueryRequest("Q1", "Q1", query))

    @Test
    fun `IndexProvider serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(sample) { Query(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(sample)
        val recreatedObject = createObjectFromJsonString(jsonString) { Query.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should deserialize json object using parser`() {
        val jsonString =  "{\"type\":\"test\",\"query\":\"{\\\"id\\\":\\\"Q1\\\",\\\"storageType\\\":\\\"_volatile\\\",\\\"queryType\\\":\\\"concrete\\\",\\\"name\\\":\\\"Q1\\\",\\\"type\\\":\\\"v1\\\",\\\"query\\\":{\\\"ont\\\":\\\"Dragons\\\",\\\"name\\\":\\\"q2\\\",\\\"elements\\\":[{\\\"type\\\":\\\"Start\\\",\\\"eNum\\\":0,\\\"next\\\":1},{\\\"type\\\":\\\"ETyped\\\",\\\"eNum\\\":1,\\\"eTag\\\":\\\"P\\\",\\\"next\\\":2,\\\"eType\\\":\\\"Person\\\",\\\"typed\\\":\\\"Person\\\"},{\\\"type\\\":\\\"EPropGroup\\\",\\\"eNum\\\":2,\\\"props\\\":[{\\\"type\\\":\\\"EProp\\\",\\\"eNum\\\":3,\\\"pType\\\":\\\"name\\\",\\\"con\\\":{\\\"type\\\":\\\"Constraint\\\",\\\"op\\\":\\\"like\\\",\\\"expr\\\":\\\"jhon*\\\",\\\"iType\\\":\\\"[]\\\"},\\\"constraint\\\":true,\\\"projection\\\":false}],\\\"quantType\\\":\\\"all\\\"}]},\\\"ontology\\\":\\\"Dragons\\\",\\\"ttl\\\":300000,\\\"searchPlan\\\":true,\\\"planTraceOptions\\\":{\\\"level\\\":\\\"none\\\"}}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Query.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Query.parse(it) }
        }
    }

    @Test
    fun `IndexProvider should safely ignore extra field in json object`() {
        val jsonString ="{\"foo\":\"bar\",\"type\":\"test\",\"query\":\"{\\\"id\\\":\\\"Q1\\\",\\\"storageType\\\":\\\"_volatile\\\",\\\"queryType\\\":\\\"concrete\\\",\\\"name\\\":\\\"Q1\\\",\\\"type\\\":\\\"v1\\\",\\\"query\\\":{\\\"ont\\\":\\\"Dragons\\\",\\\"name\\\":\\\"q2\\\",\\\"elements\\\":[{\\\"type\\\":\\\"Start\\\",\\\"eNum\\\":0,\\\"next\\\":1},{\\\"type\\\":\\\"ETyped\\\",\\\"eNum\\\":1,\\\"eTag\\\":\\\"P\\\",\\\"next\\\":2,\\\"eType\\\":\\\"Person\\\",\\\"typed\\\":\\\"Person\\\"},{\\\"type\\\":\\\"EPropGroup\\\",\\\"eNum\\\":2,\\\"props\\\":[{\\\"type\\\":\\\"EProp\\\",\\\"eNum\\\":3,\\\"pType\\\":\\\"name\\\",\\\"con\\\":{\\\"type\\\":\\\"Constraint\\\",\\\"op\\\":\\\"like\\\",\\\"expr\\\":\\\"jhon*\\\",\\\"iType\\\":\\\"[]\\\"},\\\"constraint\\\":true,\\\"projection\\\":false}],\\\"quantType\\\":\\\"all\\\"}]},\\\"ontology\\\":\\\"Dragons\\\",\\\"ttl\\\":300000,\\\"searchPlan\\\":true,\\\"planTraceOptions\\\":{\\\"level\\\":\\\"none\\\"}}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Query.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
