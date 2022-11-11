/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.STRING_READER
import org.opensearch.commons.utils.STRING_WRITER
import org.opensearch.commons.utils.enumReader
import org.opensearch.commons.utils.logger
import org.opensearch.graph.model.ObjectType
import org.opensearch.graph.model.RestTag.QUERY_PARAM_LIST_FIELD
import org.opensearch.graph.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.graph.settings.PluginSettings
import org.opensearch.search.sort.SortOrder
import java.io.IOException
import java.util.EnumSet

/**
 * Action Request running a query.
 */
class RunQueryRequest : ActionRequest, ToXContentObject {
    val queryId: String
    val maxItems: Int
    val queryParams: Map<String, String>

    companion object {
        private val log by logger(RunQueryRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { RunQueryRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): RunQueryRequest {
            var queryId = "?"
            var maxItems = PluginSettings.defaultMaxFetchCount
            var queryParams: Map<String, String> = mapOf()

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    OBJECT_ID_FIELD -> queryId = parser.text()
                    MAX_ITEMS_FIELD -> maxItems = parser.intValue()
                    QUERY_PARAM_LIST_FIELD -> queryParams = parser.mapStrings()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing GetObjectRequest")
                    }
                }
            }
            return RunQueryRequest(
                queryId,
                maxItems,
                queryParams
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(OBJECT_ID_FIELD, queryId)
            .field(MAX_ITEMS_FIELD, maxItems)
            .field(QUERY_PARAM_LIST_FIELD, queryParams)
            .endObject()
    }

    @Suppress("LongParameterList")
    constructor(
            queryId: String,
            maxItems: Int = PluginSettings.defaultMaxFetchCount,
            queryParams: Map<String, String> = mapOf()
    ) {
        this.queryId = queryId
        this.maxItems = maxItems
        this.queryParams = queryParams
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        queryId = input.readString()
        maxItems = input.readInt()
        queryParams = input.readMap(STRING_READER, STRING_READER)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeString(queryId)
        output.writeInt(maxItems)
        output.writeMap(queryParams, STRING_WRITER, STRING_WRITER)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (maxItems <= 0) {
            validationException = ValidateActions.addValidationError("maxItems is not +ve", validationException)
        }
        return validationException
    }
}
