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
     * Setting to choose admin access restriction.
     */
    private const val ADMIN_ACCESS_KEY = "$ACCESS_KEY_PREFIX.adminAccess"

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
     * Minimum number of items to query.
     */
    private const val MINIMUM_ITEMS_QUERY_COUNT = 10

    /**
     * Default admin access method.
     */
    private const val DEFAULT_ADMIN_ACCESS_METHOD = "AllKGraphObjects"

    /**
     * Default filter-by method.
     */
    private const val DEFAULT_FILTER_BY_METHOD = "NoFilter"

    /**
     * Default filter-by method.
     */
    private val DEFAULT_IGNORED_ROLES = listOf(
        "own_index",
    )

    /**
     * Operation timeout setting in ms for I/O operations
     */
    @Volatile
    var operationTimeoutMs: Long

    /**
     * Default number of items to query.
     */
    @Volatile
    var defaultMaxFetchCount: Int

    /**
     * admin access method.
     */
    @Volatile
    var adminAccess: AdminAccess

    /**
     * Filter-by method.
     */
    @Volatile
    var filterBy: FilterBy

    /**
     * list of ignored roles.
     */
    @Volatile
    var ignoredRoles: List<String>

    /**
     * Enum for types of admin access
     * "Standard" -> Admin user access follows standard user
     * "AllKGraphObjects" -> Admin user with "all_access" role can see all objects of all users.
     */
    internal enum class AdminAccess { Standard, AllObjects }

    /**
     * Enum for types of filterBy options
     * NoFilter -> everyone see each other's items
     * User -> items are visible to only themselves
     * Roles -> items are visible to users having any one of the role of creator
     * BackendRoles -> items are visible to users having any one of the backend role of creator
     */
    internal enum class FilterBy { NoFilter, User, Roles, BackendRoles }

    private const val DECIMAL_RADIX: Int = 10

    private val log = LogManager.getLogger(javaClass)
    private val defaultSettings: Map<String, String>

    init {
        var settings: Settings? = null
        val configDirName = BootstrapInfo.getSystemProperties()?.get("opensearch.path.conf")?.toString()
        if (configDirName != null) {
            val defaultSettingYmlFile = Path.of(configDirName, PLUGIN_NAME, "graph.yml")
            try {
                settings = Settings.builder().loadFromPath(defaultSettingYmlFile).build()
            } catch (exception: IOException) {
                log.warn("$LOG_PREFIX:Failed to load ${defaultSettingYmlFile.toAbsolutePath()}")
            }
        }
        // Initialize the settings values to default values
        operationTimeoutMs = (settings?.get(OPERATION_TIMEOUT_MS_KEY)?.toLong()) ?: DEFAULT_OPERATION_TIMEOUT_MS
        defaultMaxFetchCount = (settings?.get(DEFAULT_ITEMS_QUERY_COUNT_KEY)?.toInt())
            ?: DEFAULT_ITEMS_QUERY_COUNT_VALUE
        adminAccess = AdminAccess.valueOf(settings?.get(ADMIN_ACCESS_KEY) ?: DEFAULT_ADMIN_ACCESS_METHOD)
        filterBy = FilterBy.valueOf(settings?.get(FILTER_BY_KEY) ?: DEFAULT_FILTER_BY_METHOD)
        ignoredRoles = settings?.getAsList(IGNORE_ROLE_KEY) ?: DEFAULT_IGNORED_ROLES

        defaultSettings = mapOf(
            OPERATION_TIMEOUT_MS_KEY to operationTimeoutMs.toString(DECIMAL_RADIX),
            DEFAULT_ITEMS_QUERY_COUNT_KEY to defaultMaxFetchCount.toString(DECIMAL_RADIX),
            ADMIN_ACCESS_KEY to adminAccess.name,
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

    private val ADMIN_ACCESS: Setting<String> = Setting.simpleString(
        ADMIN_ACCESS_KEY,
        defaultSettings[ADMIN_ACCESS_KEY]!!,
        NodeScope, Dynamic
    )

    private val FILTER_BY: Setting<String> = Setting.simpleString(
        FILTER_BY_KEY,
        defaultSettings[FILTER_BY_KEY]!!,
        NodeScope, Dynamic
    )

    private val IGNORED_ROLES: Setting<List<String>> = Setting.listSetting(
        IGNORE_ROLE_KEY,
        DEFAULT_IGNORED_ROLES,
        { it },
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
            ADMIN_ACCESS,
            FILTER_BY,
            IGNORED_ROLES
        )
    }

    /**
     * Update the setting variables to setting values from local settings
     * @param clusterService cluster service instance
     */
    private fun updateSettingValuesFromLocal(clusterService: ClusterService) {
        operationTimeoutMs = OPERATION_TIMEOUT_MS.get(clusterService.settings)
        defaultMaxFetchCount = DEFAULT_ITEMS_QUERY_COUNT.get(clusterService.settings)
        adminAccess = AdminAccess.valueOf(ADMIN_ACCESS.get(clusterService.settings))
        filterBy = FilterBy.valueOf(FILTER_BY.get(clusterService.settings))
        ignoredRoles = IGNORED_ROLES.get(clusterService.settings)
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
            defaultMaxFetchCount = clusterDefaultItemsQueryCount
        }
        val clusterAdminAccess = clusterService.clusterSettings.get(ADMIN_ACCESS)
        if (clusterAdminAccess != null) {
            log.debug("$LOG_PREFIX:$ADMIN_ACCESS_KEY -autoUpdatedTo-> $clusterAdminAccess")
            adminAccess = AdminAccess.valueOf(clusterAdminAccess)
        }
        val clusterFilterBy = clusterService.clusterSettings.get(FILTER_BY)
        if (clusterFilterBy != null) {
            log.debug("$LOG_PREFIX:$FILTER_BY_KEY -autoUpdatedTo-> $clusterFilterBy")
            filterBy = FilterBy.valueOf(clusterFilterBy)
        }
        val clusterIgnoredRoles = clusterService.clusterSettings.get(IGNORED_ROLES)
        if (clusterIgnoredRoles != null) {
            log.debug("$LOG_PREFIX:$IGNORE_ROLE_KEY -autoUpdatedTo-> $clusterIgnoredRoles")
            ignoredRoles = clusterIgnoredRoles
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
            defaultMaxFetchCount = it
            log.info("$LOG_PREFIX:$DEFAULT_ITEMS_QUERY_COUNT_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(ADMIN_ACCESS) {
            adminAccess = AdminAccess.valueOf(it)
            log.info("$LOG_PREFIX:$ADMIN_ACCESS_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(FILTER_BY) {
            filterBy = FilterBy.valueOf(it)
            log.info("$LOG_PREFIX:$FILTER_BY_KEY -updatedTo-> $it")
        }
        clusterService.clusterSettings.addSettingsUpdateConsumer(IGNORED_ROLES) {
            ignoredRoles = it
            log.info("$LOG_PREFIX:$IGNORE_ROLE_KEY -updatedTo-> $it")
        }
    }
}
