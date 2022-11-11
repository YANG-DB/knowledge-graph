/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.model

import org.opensearch.commons.utils.EnumParser
import org.opensearch.graph.model.RestTag.QUERY_FIELD
import java.util.*

/**
 * Enum for KGraphObject type
 */
enum class ObjectType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    QUERY(QUERY_FIELD) {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get ConfigType from tag or NONE if not found
         * @param tag the tag
         * @return ConfigType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): ObjectType {
            return tagMap[tag] ?: NONE
        }

        fun getAll(): EnumSet<ObjectType> {
            val allTypes = EnumSet.allOf(ObjectType::class.java)
            allTypes.remove(NONE)
            return allTypes
        }
    }
}
