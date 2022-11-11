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
import org.opensearch.common.xcontent.*
import org.opensearch.commons.utils.*
import org.opensearch.graph.model.ObjectType
import org.opensearch.graph.model.RestTag.FILTER_PARAM_LIST_FIELD
import org.opensearch.graph.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_ID_LIST_FIELD
import org.opensearch.graph.model.RestTag.OBJECT_TYPE_FIELD
import org.opensearch.graph.model.RestTag.SORT_FIELD_FIELD
import org.opensearch.graph.model.RestTag.SORT_ORDER_FIELD
import org.opensearch.search.sort.SortOrder
import java.io.IOException
import java.util.*

/**
 * Action Request for getting Object.
 */
class QueryInfoObjectRequest : ActionRequest, ToXContentObject {
    val objectIds: Set<String>
    val types: EnumSet<ObjectType>
    val fromIndex: Int
    val sortField: String?
    val sortOrder: SortOrder?
    val filterParams: Map<String, String>

    companion object {
        private val log by logger(QueryInfoObjectRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { QueryInfoObjectRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): QueryInfoObjectRequest {
            var objectIdList: Set<String> = setOf()
            var types: EnumSet<ObjectType> = EnumSet.noneOf(ObjectType::class.java)
            var fromIndex = 0
            var sortField: String? = null
            var sortOrder: SortOrder? = null
            var filterParams: Map<String, String> = mapOf()

            XContentParserUtils.ensureExpectedToken(
                    XContentParser.Token.START_OBJECT,
                    parser.currentToken(),
                    parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    OBJECT_ID_LIST_FIELD -> objectIdList = parser.stringList().toSet()
                    OBJECT_TYPE_FIELD -> types = parser.enumSet(ObjectType.enumParser)
                    FROM_INDEX_FIELD -> fromIndex = parser.intValue()
                    SORT_FIELD_FIELD -> sortField = parser.text()
                    SORT_ORDER_FIELD -> sortOrder = SortOrder.fromString(parser.text())
                    FILTER_PARAM_LIST_FIELD -> filterParams = parser.mapStrings()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing GetObjectRequest")
                    }
                }
            }
            return QueryInfoObjectRequest(
                    objectIdList,
                    types,
                    fromIndex,
                    sortField,
                    sortOrder,
                    filterParams
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
                .field(OBJECT_ID_LIST_FIELD, objectIds)
                .field(OBJECT_TYPE_FIELD, types)
                .field(FROM_INDEX_FIELD, fromIndex)
                .fieldIfNotNull(SORT_FIELD_FIELD, sortField)
                .fieldIfNotNull(SORT_ORDER_FIELD, sortOrder)
                .field(FILTER_PARAM_LIST_FIELD, filterParams)
                .endObject()
    }

    @Suppress("LongParameterList")
    constructor(
            objectIds: Set<String> = setOf(),
            types: EnumSet<ObjectType> = EnumSet.noneOf(ObjectType::class.java),
            fromIndex: Int = 0,
            sortField: String? = null,
            sortOrder: SortOrder? = null,
            filterParams: Map<String, String> = mapOf()
    ) {
        this.objectIds = objectIds
        this.types = types
        this.fromIndex = fromIndex
        this.sortField = sortField
        this.sortOrder = sortOrder
        this.filterParams = filterParams
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectIds = input.readStringList().toSet()
        types = input.readEnumSet(ObjectType::class.java)
        fromIndex = input.readInt()
        sortField = input.readOptionalString()
        sortOrder = input.readOptionalWriteable(enumReader(SortOrder::class.java))
        filterParams = input.readMap(STRING_READER, STRING_READER)
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeStringCollection(objectIds)
        output.writeEnumSet(types)
        output.writeInt(fromIndex)
        output.writeOptionalString(sortField)
        output.writeOptionalWriteable(sortOrder)
        output.writeMap(filterParams, STRING_WRITER, STRING_WRITER)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (fromIndex < 0) {
            validationException = ValidateActions.addValidationError("fromIndex is -ve", validationException)
        }
        return validationException
    }
}
