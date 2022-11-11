/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.scheduler

import org.opensearch.common.xcontent.XContentParser
import org.opensearch.jobscheduler.spi.JobDocVersion
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.ScheduledJobParser
import org.opensearch.graph.model.ScheduledJobDoc

internal object KGraphJobParser : ScheduledJobParser {
    /**
     * {@inheritDoc}
     */
    override fun parse(xContentParser: XContentParser, id: String, jobDocVersion: JobDocVersion): ScheduledJobParameter {
        xContentParser.nextToken()
        return ScheduledJobDoc.parse(xContentParser, id)
    }
}
