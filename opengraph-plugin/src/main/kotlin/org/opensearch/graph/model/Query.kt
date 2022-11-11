package org.opensearch.graph.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.*
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.LOG_PREFIX
import org.opensearch.graph.model.transport.CreateQueryRequest
import org.opensearch.graph.util.logger

/**
 * This element represents a physical indices' entity which is stored as a document in the DB
 * The entity has a type, name, description, ontology and the actual content field which contains the Index-Provider SDL Json
 * The indices represent list of indices that are described in this Physical index-provider
 * physical
 */
internal data class Query(
        val type: String,
        val content: CreateQueryRequest,
) : BaseObjectData {

    internal companion object {
        private val log by logger(Query::class.java)
        private const val TYPE_TAG = "type"
        private const val CONTENT_TAG = "query"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Query(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create SchemaEntity
         * @param parser data referenced at parser
         * @return created SchemaEntity object
         */
        fun parse(parser: XContentParser): Query {
            var type = "Undefined"
            var content = CreateQueryRequest()
            XContentParserUtils.ensureExpectedToken(
                    XContentParser.Token.START_OBJECT,
                    parser.currentToken(),
                    parser
            )
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    TYPE_TAG -> type = parser.text()
                    CONTENT_TAG -> content = ObjectMapper().readValue(parser.text(), object : TypeReference<CreateQueryRequest>() {})
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:SchemaType Skipping Unknown field $fieldName")
                    }
                }
            }
            return Query(type, content)
        }
    }


    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
            type = input.readString(),
            content = ObjectMapper().readValue(input.readString(), object : TypeReference<CreateQueryRequest>() {})
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeString(ObjectMapper().writeValueAsString(content))
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
                .field(TYPE_TAG, type)
                .field(CONTENT_TAG, ObjectMapper().writeValueAsString(content))
        return builder.endObject()
    }
}
