/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.graph.settings

import org.apache.logging.log4j.LogManager
import org.opensearch.bootstrap.BootstrapInfo
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.settings.Setting
import org.opensearch.common.settings.Setting.Property.Dynamic
import org.opensearch.common.settings.Setting.Property.NodeScope
import org.opensearch.common.settings.Settings
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.LOG_PREFIX
import org.opensearch.graph.KnowledgeGraphPlugin.Companion.PLUGIN_NAME
import java.io.IOException
import java.nio.file.Path

/**
 * settings specific to KGraph Plugin.
 */
internal object PluginSettings {

    /**
     * Settings Key prefix for this plugin.
     */
    private const val KEY_PREFIX = "opensearch.kgraph"

    /**
     * General settings Key prefix.
     */
    private const val GENERAL_KEY_PREFIX = "$KEY_PREFIX.general"

    /**
     * Access settings Key prefix.
     */
    private const val ACCESS_KEY_PREFIX = "$KEY_PREFIX.access"

    /**
     * Operation timeout for network operations.
     */
    private const val OPERATION_TIMEOUT_MS_KEY = "$GENERAL_KEY_PREFIX.operationTimeoutMs"

    /**
     * Setting to choose default number of items to query.
     */
    private const val DEFAULT_ITEMS_QUERY_COUNT_KEY = "$GENERAL_KEY_PREFIX.defaultItemsQueryCount"

    /**
     * Setting to choose default number of items to query.
     */
    private const val DEFAULT_ITEMS_FETCH_COUNT_KEY = "$GENERAL_KEY_PREFIX.defaultMaxFetchCount"


    /**
     * Setting to choose filter method.
     */
    private const val FILTER_BY_KEY = "$ACCESS_KEY_PREFIX.filterBy"

    /**
     * Setting to choose ignored roles for filtering.
     */
    private const val IGNORE_ROLE_KEY = "$ACCESS_KEY_PREFIX.ignoreRoles"

    /**
     * Default operation timeout for network operations.
     */
    private const val DEFAULT_OPERATION_TIMEOUT_MS = 60000L

    /**
     * Minimum operation timeout for network operations.
     */
    private const val MINIMUM_OPERATION_TIMEOUT_MS = 100L

    /**
     * Default number of items to query.
     */
    private const val DEFAULT_ITEMS_QUERY_COUNT_VALUE = 1000

    /**
     * Default number of items to fetch.
     */
    private const val DEFAULT_ITEMS_FETCH_COUNT_VALUE = 10000

    /**
     * Minimum number of items to query.
     */
    private const val MINIMUM_ITEMS_QUERY_COUNT = 10

    /**
     * Minimum number of items fetch per query execution.
     */
    private const val MINIMUM_ITEMS_FETCH_COUNT = 1000

    /**
     * Default filter-by method.
     */
    private const val DEFAULT_FILTER_BY_METHOD = "NoFilter"


    /**
     * Operation timeout setting in ms for I/O operations
     */
    @Volatile
    var operationTimeoutMs: Long

    /**
     * Filter-by method.
     */
    @Volatile
    var filterBy: FilterBy


    /**
     * Default number of items to query.
     */
    @Volatile
    var defaultMaxFetchCount: Int

    /**
     * Default number of items to query.
     */
    @Volatile
    var defaultMaxQueryCount: Int


    private const val DECIMAL_RADIX: Int = 10

    private val log = LogManager.getLogger(javaClass)
    private val defaultSettings: Map<String, String>

    /**
     * Enum for types of filterBy options
     * NoFilter -> everyone see each other's items
     * User -> items are visible to only themselves
     * Roles -> items are visible to users having any one of the role of creator
     * BackendRoles -> items are visible to users having any one of the backend role of creator
     */
    internal enum class FilterBy { NoFilter, User, Roles, BackendRoles }

    init {
        var settings: Settings? = null
        var defaultSettingYmlFile = Path.of(PLUGIN_NAME, "knowledge-graph.yml")
        val configDirName = BootstrapInfo.getSystemProperties()?.get("opensearch.path.conf")?.toString()

        if (configDirName != null) {
            defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "knowledge-graph.yml")
        }
        try {
            settings = Settings.builder().loadFromPath(defaultSettingYmlFile).build()
        } catch (exception: IOException) {
            log.warn("$LOG_PREFIX:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}")
        }

        // Initialize the settings values to default values
        operationTimeoutMs = (settings?.get(OPERATION_TIMEOUT_MS_KEY)?.toLong()) ?: DEFAULT_OPERATION_TIMEOUT_MS
        filterBy = FilterBy.valueOf(settings?.get(FILTER_BY_KEY) ?: DEFAULT_FILTER_BY_METHOD)
        defaultMaxQueryCount = (settings?.get(DEFAULT_ITEMS_QUERY_COUNT_KEY)?.toInt())
                ?: DEFAULT_ITEMS_QUERY_COUNT_VALUE
        defaultMaxFetchCount = (settings?.get(DEFAULT_ITEMS_FETCH_COUNT_KEY)?.toInt())
                ?: DEFAULT_ITEMS_FETCH_COUNT_VALUE

        defaultSettings = mapOf(
                OPERATION_TIMEOUT_MS_KEY to operationTimeoutMs.toString(DECIMAL_RADIX),
                DEFAULT_ITEMS_QUERY_COUNT_KEY to defaultMaxQueryCount.toString(DECIMAL_RADIX),
                DEFAULT_ITEMS_FETCH_COUNT_KEY to defaultMaxFetchCount.toString(DECIMAL_RADIX),
                FILTER_BY_KEY to filterBy.name
        )
    }

    private val OPERATION_TIMEOUT_MS: Setting<Long> = Setting.longSetting(
            OPERATION_TIMEOUT_MS_KEY,
            defaultSettings[OPERATION_TIMEOUT_MS_KEY]!!.toLong(),
            MINIMUM_OPERATION_TIMEOUT_MS,
            NodeScope, Dynamic
    )


    private val DEFAULT_ITEMS_QUERY_COUNT: Setting<Int> = Setting.intSetting(
            DEFAULT_ITEMS_QUERY_COUNT_KEY,
            defaultSettings[DEFAULT_ITEMS_QUERY_COUNT_KEY]!!.toInt(),
            MINIMUM_ITEMS_QUERY_COUNT,
            NodeScope, Dynamic
    )

    private val DEFAULT_ITEMS_FETCH_COUNT: Setting<Int> = Setting.intSetting(
            DEFAULT_ITEMS_FETCH_COUNT_KEY,
            defaultSettings[DEFAULT_ITEMS_FETCH_COUNT_KEY]!!.toInt(),
            MINIMUM_ITEMS_FETCH_COUNT,
            NodeScope, Dynamic
    )


    private val FILTER_BY: Setting<String> = Setting.simpleString(
            FILTER_BY_KEY,
            defaultSettings[FILTER_BY_KEY]!!,
            NodeScope, Dynamic
    )

    /**
     * Returns list of additional settings available specific to this plugin.
     *
     * @return list of settings defined in this plugin
     */
    fun getAllSettings(): List<Setting<*>> {
        return listOf(
                OPERATION_TIMEOUT_MS,
                DEFAULT_ITEMS_QUERY_COUNT,
                DEFAULT_ITEMS_FETCH_COUNT,
                FILTER_BY
        )
    }

    /**
     * Update the setting variables to setting values from local settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromLocal(clusterService: ClusterService) {
        operationTimeoutMs = OPERATION_TIMEOUT_MS.get(clusterService.settings)
        defaultMaxQueryCount = DEFAULT_ITEMS_QUERY_COUNT.get(clusterService.settings)
        defaultMaxFetchCount = DEFAULT_ITEMS_FETCH_COUNT.get(clusterService.settings)
    }

    /**
     * Update the setting variables to setting values from cluster settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromCluster(clusterService: ClusterService) {
        val clusterOperationTimeoutMs = clusterService.clusterSettings.get(OPERATION_TIMEOUT_MS)
        if (clusterOperationTimeoutMs != null) {
            log.debug("$LOG_PREFIX:$OPERATION_TIMEOUT_MS_KEY -autoUpdatedTo-> $clusterOperationTimeoutMs")
            operationTimeoutMs = clusterOperationTimeoutMs
        }
        val clusterDefaultItemsQueryCount = clusterService.clusterSettings.get(DEFAULT_ITEMS_QUERY_COUNT)
        if (clusterDefaultItemsQueryCount != null) {
            log.debug("$LOG_PREFIX:$DEFAULT_ITEMS_QUERY_COUNT_KEY -autoUpdatedTo-> $clusterDefaultItemsQueryCount")
            defaultMaxQueryCount = clusterDefaultItemsQueryCount
        }
        val clusterDefaultItemsFetchCount = clusterService.clusterSettings.get(DEFAULT_ITEMS_FETCH_COUNT)
        if (clusterDefaultItemsFetchCount != null) {
            log.debug("$LOG_PREFIX:$DEFAULT_ITEMS_FETCH_COUNT_KEY -autoUpdatedTo-> $clusterDefaultItemsFetchCount")
            defaultMaxFetchCount = clusterDefaultItemsFetchCount
        }
    }

    /**
     * adds Settings update listeners to all settings.
     * @param clusterService cluster service instance
     */
    fun addSettingsUpdateConsumer(clusterService: ClusterService) {
        updateSettingValuesFromLocal(clusterService)
        // Update the variables to cluster setting values
        // If the cluster is not yet started then we get default values again
        updateSettingValuesFromCluster(clusterService)

        clusterService.clusterSettings.addSettingsUpdateConsumer(OPERATION_TIMEOUT_MS) {
            operationTimeoutMs = it
            log.info("$LOG_PREFIX:$OPERATION_TIMEOUT_MS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(DEFAULT_ITEMS_QUERY_COUNT) {
            defaultMaxQueryCount = it
            log.info("$LOG_PREFIX:$DEFAULT_ITEMS_QUERY_COUNT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(DEFAULT_ITEMS_FETCH_COUNT) {
            defaultMaxFetchCount = it
            log.info("$LOG_PREFIX:$DEFAULT_ITEMS_FETCH_COUNT_KEY -updatedTo-> $it")
        }
    }
}
