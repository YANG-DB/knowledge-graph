/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.transport.TransportService

/**
 * Get Object transport action
 */
internal class QueryInfoAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<RunQueryRequest, RunQueryResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::RunQueryRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/graph/query/info"
        internal val ACTION_TYPE = ActionType(NAME, ::RunQueryResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RunQueryRequest, user: User?): RunQueryResponse {
        return GraphActions.run(request, user)
    }
}
