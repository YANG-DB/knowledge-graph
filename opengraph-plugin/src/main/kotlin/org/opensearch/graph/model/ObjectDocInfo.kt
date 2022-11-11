package org.opensearch.graph.model

import org.opensearch.index.seqno.SequenceNumbers

/**
 * Class for storing the KGraphObject document with document properties.
 */
data class ObjectDocInfo(
    val id: String? = null,
    val version: Long = -1L,
    val seqNo: Long = SequenceNumbers.UNASSIGNED_SEQ_NO,
    val primaryTerm: Long = SequenceNumbers.UNASSIGNED_PRIMARY_TERM,
    val objectDoc: ObjectDoc
)
