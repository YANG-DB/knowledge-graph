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
 * Create KGraphObject transport action
 */
internal class CreateQueryAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateQueryRequest, CreateQueryResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::CreateQueryRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/graph/query/create"
        internal val ACTION_TYPE = ActionType(NAME, ::CreateQueryResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
            request: CreateQueryRequest,
            user: User?
    ): CreateQueryResponse {
        return GraphActions.create(request, user)
    }
}
