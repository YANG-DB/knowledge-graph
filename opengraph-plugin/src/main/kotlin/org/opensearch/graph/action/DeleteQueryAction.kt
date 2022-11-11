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
 * Delete Object transport action
 */
internal class DeleteQueryAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<DeleteQueryRequest, DeleteQueryResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::DeleteQueryRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/graph/query/delete"
        internal val ACTION_TYPE = ActionType(NAME, ::DeleteQueryResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: DeleteQueryRequest, user: User?): DeleteQueryResponse {

        return GraphActions.delete(request.objectIds, user)
    }
}
