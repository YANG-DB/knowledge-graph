/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.graph

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionResponse
import org.opensearch.client.Client
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.node.DiscoveryNodes
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.*
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.graph.action.CreateQueryAction
import org.opensearch.graph.action.DeleteQueryAction
import org.opensearch.graph.action.QueryInfoAction
import org.opensearch.graph.action.RunQueryAction
import org.opensearch.graph.index.KGraphIndex
import org.opensearch.graph.resthandler.SchedulerRestHandler
import org.opensearch.graph.resthandler.KGraphRestHandler
import org.opensearch.graph.scheduler.KGraphJobParser
import org.opensearch.graph.scheduler.KGraphJobRunner
import org.opensearch.graph.settings.PluginSettings
import org.opensearch.jobscheduler.spi.JobSchedulerExtension
import org.opensearch.jobscheduler.spi.ScheduledJobParser
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.plugins.ActionPlugin
import org.opensearch.plugins.Plugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.rest.RestController
import org.opensearch.rest.RestHandler
import org.opensearch.script.ScriptService
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier


/**
 * Entry point of the OpenSearch KGraph plugin.
 * This class initializes the rest handlers.
 */
class KnowledgeGraphPlugin : Plugin(), ActionPlugin, JobSchedulerExtension {

    companion object {
        const val PLUGIN_NAME = "knowledge-graph"
        const val LOG_PREFIX = "graph"
        const val BASE_KGRAPH_URI = "/_plugins/_knowledgegraph"
    }

    /**
     * {@inheritDoc}
     */
    override fun getSettings(): List<Setting<*>> {
        return PluginSettings.getAllSettings()
    }

    /**
     * {@inheritDoc}
     */
    override fun createComponents(
        client: Client,
        clusterService: ClusterService,
        threadPool: ThreadPool,
        resourceWatcherService: ResourceWatcherService,
        scriptService: ScriptService,
        xContentRegistry: NamedXContentRegistry,
        environment: Environment,
        nodeEnvironment: NodeEnvironment,
        namedWriteableRegistry: NamedWriteableRegistry,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        repositoriesServiceSupplier: Supplier<RepositoriesService>
    ): Collection<Any> {
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        KGraphIndex.initialize(client, clusterService)
        return emptyList()
    }

    /**
     * {@inheritDoc}
     */
    override fun getRestHandlers(
        settings: Settings,
        restController: RestController,
        clusterSettings: ClusterSettings,
        indexScopedSettings: IndexScopedSettings,
        settingsFilter: SettingsFilter,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        nodesInCluster: Supplier<DiscoveryNodes>
    ): List<RestHandler> {
        return listOf(
            KGraphRestHandler(),
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getActions(): List<ActionPlugin.ActionHandler<out ActionRequest, out ActionResponse>> {
        return listOf(
            ActionPlugin.ActionHandler(
                CreateQueryAction.ACTION_TYPE,
                CreateQueryAction::class.java
            ),
            ActionPlugin.ActionHandler(
                DeleteQueryAction.ACTION_TYPE,
                DeleteQueryAction::class.java
            ),
            ActionPlugin.ActionHandler(
                RunQueryAction.ACTION_TYPE,
                RunQueryAction::class.java
            ),
            ActionPlugin.ActionHandler(
                QueryInfoAction.ACTION_TYPE,
                QueryInfoAction::class.java
            )
        )
    }

    override fun getJobType(): String {
        return "graph"
    }

    override fun getJobIndex(): String {
        return SchedulerRestHandler.SCHEDULED_JOB_INDEX
    }

    override fun getJobRunner(): ScheduledJobRunner {
        return KGraphJobRunner
    }

    override fun getJobParser(): ScheduledJobParser {
        return KGraphJobParser
    }
}
