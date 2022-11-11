/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.opensearch.common.xcontent.*
import org.opensearch.graph.model.ObjectDoc
import org.opensearch.graph.model.ObjectType
import org.opensearch.graph.model.Query
import org.opensearch.graph.model.query.Start
import org.opensearch.graph.model.query.entity.ETyped
import org.opensearch.graph.model.query.properties.EProp
import org.opensearch.graph.model.query.properties.EPropGroup
import org.opensearch.graph.model.query.properties.constraint.Constraint
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp
import org.opensearch.graph.model.transport.CreateQueryRequest
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.*

private const val DEFAULT_TIME_ACCURACY_SEC = 5L

var ontology = "{}"

var schemaEntityType = "type Author {\n" +
    "    name: String!\n" +
    "    born: DateTime!\n" +
    "    died: DateTime\n" +
    "    nationality: String!\n" +
    "    books: [Book]\n" +
    "}"

var query = org.opensearch.graph.model.query.Query.Builder.instance().withName("q2").withOnt("Dragons")
        .withElements(Arrays.asList(
                Start(0, 1),
                ETyped(1, "P", "Person", 2, 0),
                EPropGroup(2,
                        EProp(3, "name", Constraint.of(ConstraintOp.like, "jhon*")))
        )).build()


fun constructQueryObjectDoc(
    name: String = "test query entity type",
    id: String = "test-id"
): ObjectDoc {
    return ObjectDoc(
            id,
            Instant.ofEpochMilli(1638482208790),
            Instant.ofEpochMilli(1638482208790),
            "test-tenant",
            listOf("test-access"),
            ObjectType.QUERY,
            Query("test", CreateQueryRequest(id, name, query)))
}

fun constructQueryTypeRequest(name: String = "test query entity"): String {
    return """
        {
            "query":{
                "name" : "$name",
                "type" : "${ObjectType.QUERY}",
                "query": "{\"id\":\"Q1\",\"storageType\":\"_volatile\",\"queryType\":\"concrete\",\"name\":\"Q1\",\"type\":\"v1\",\"query\":{\"ont\":\"Dragons\",\"name\":\"q2\",\"elements\":[{\"type\":\"Start\",\"eNum\":0,\"next\":1},{\"type\":\"ETyped\",\"eNum\":1,\"eTag\":\"P\",\"next\":2,\"eType\":\"Person\",\"typed\":\"Person\"},{\"type\":\"EPropGroup\",\"eNum\":2,\"props\":[{\"type\":\"EProp\",\"eNum\":3,\"pType\":\"name\",\"con\":{\"type\":\"Constraint\",\"op\":\"like\",\"expr\":\"jhon*\",\"iType\":\"[]\"},\"constraint\":true,\"projection\":false}],\"quantType\":\"all\"}]},\"ontology\":\"Dragons\",\"ttl\":300000,\"searchPlan\":true,\"planTraceOptions\":{\"level\":\"none\"}}"
                }
            }
    """.trimIndent()
}


fun jsonify(text: String): JsonObject {
    return JsonParser.parseString(text).asJsonObject
}

fun validateErrorResponse(response: JsonObject, statusCode: Int, errorType: String = "status_exception") {
    Assert.assertNotNull("Error response content should be generated", response)
    val status = response.get("status").asInt
    val error = response.get("error").asJsonObject
    val rootCause = error.get("root_cause").asJsonArray
    val type = error.get("type").asString
    val reason = error.get("reason").asString
    Assert.assertEquals(statusCode, status)
    Assert.assertEquals(errorType, type)
    Assert.assertNotNull(reason)
    Assert.assertNotNull(rootCause)
    Assert.assertTrue(rootCause.size() > 0)
}

fun getJsonString(xContent: ToXContent, params: ToXContent.Params? = ToXContent.EMPTY_PARAMS): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
        xContent.toXContent(builder, params)
        builder.close()
        return byteArrayOutputStream.toString("UTF8")
    }
}

inline fun <reified CreateType> createObjectFromJsonString(
    jsonString: String,
    block: (XContentParser) -> CreateType
): CreateType {
    val parser = XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, jsonString)
    parser.nextToken()
    return block(parser)
}
