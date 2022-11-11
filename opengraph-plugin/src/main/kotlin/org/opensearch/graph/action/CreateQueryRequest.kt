/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.graph.model.BaseObjectData
import org.opensearch.graph.model.ObjectDataProperties
import org.opensearch.graph.model.ObjectType
import org.opensearch.graph.model.RestTag.OBJECT_ID_FIELD
import java.io.IOException

/**
 * Action request for creating new configuration.
 */
internal class CreateQueryRequest : ActionRequest, ToXContentObject {
    val objectId: String?
    val type: ObjectType
    val objectData: BaseObjectData?

    companion object {
        private val log by logger(CreateQueryRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateQueryRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         * @param id optional id to use if missed in XContent
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser, id: String? = null): CreateQueryRequest {
            var objectId: String? = id
            var type: ObjectType? = null
            var baseObjectData: BaseObjectData? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    OBJECT_ID_FIELD -> objectId = parser.text()
                    else -> {
                        val objectTypeForTag = ObjectType.fromTagOrDefault(fieldName)
                        if (objectTypeForTag != ObjectType.NONE && baseObjectData == null) {
                            baseObjectData = ObjectDataProperties.createObjectData(objectTypeForTag, parser)
                            type = objectTypeForTag
                        } else {
                            parser.skipChildren()
                            log.info("Unexpected field: $fieldName, while parsing CreateObjectRequest")
                        }
                    }
                }
            }
            type ?: throw IllegalArgumentException("Object type field absent")
            baseObjectData ?: throw IllegalArgumentException("Object data field absent")
            return CreateQueryRequest(objectId, type, baseObjectData)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .fieldIfNotNull(OBJECT_ID_FIELD, objectId)
            .field(type.tag, objectData)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param objectId optional id to use for Object
     * @param type type of Object
     * @param objectData the Object
     */
    constructor(objectId: String? = null, type: ObjectType, objectData: BaseObjectData) {
        this.objectId = objectId
        this.type = type
        this.objectData = objectData
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readOptionalString()
        type = input.readEnum(ObjectType::class.java)
        objectData = input.readOptionalWriteable(
            ObjectDataProperties.getReaderForObjectType(
                input.readEnum(
                    ObjectType::class.java
                )
            )
        )
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        super.writeTo(output)
        output.writeOptionalString(objectId)
        output.writeEnum(type)
        output.writeEnum(type)
        output.writeOptionalWriteable(objectData)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
