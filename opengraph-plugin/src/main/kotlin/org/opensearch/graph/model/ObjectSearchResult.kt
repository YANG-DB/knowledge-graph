/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.model

import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.graph.model.RestTag.OBJECT_LIST_FIELD

/**
 * KGraphObject search results
 */
internal class ObjectSearchResult : SearchResults<ObjectDoc> {

    /**
     * single item result constructor
     */
    constructor(objectItem: ObjectDoc) : super(OBJECT_LIST_FIELD, objectItem)

    /**
     * multiple items result constructor
     */
    constructor(objectList: List<ObjectDoc>) : this(
        0,
        objectList.size.toLong(),
        TotalHits.Relation.EQUAL_TO,
        objectList
    )

    /**
     * all param constructor
     */
    constructor(
        startIndex: Long,
        totalHits: Long,
        totalHitRelation: TotalHits.Relation,
        objectList: List<ObjectDoc>
    ) : super(startIndex, totalHits, totalHitRelation, OBJECT_LIST_FIELD, objectList)

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : super(input, ObjectDoc.reader)

    /**
     * Construct object from XContentParser
     */
    constructor(parser: XContentParser) : super(parser, OBJECT_LIST_FIELD)

    /**
     * Construct object from SearchResponse
     */
    constructor(from: Long, response: SearchResponse, searchHitParser: SearchHitParser<ObjectDoc>) : super(
        from,
        response,
        searchHitParser,
        OBJECT_LIST_FIELD
    )

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser, useId: String?): ObjectDoc {
        return ObjectDoc.parse(parser, useId)
    }
}
