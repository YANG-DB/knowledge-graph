package org.opensearch.graph.unipop.controller;





import com.typesafe.config.ConfigValue;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OpensearchGraphConfiguration extends BaseConfiguration {
    //E/S cluster props
    private Map<String, String> clusterProps = new HashMap<>();

    //region Constructor
    public OpensearchGraphConfiguration() {}

    public OpensearchGraphConfiguration(final Configuration configuration) {
        configuration.getKeys().forEachRemaining(key -> addProperty(key, configuration.getProperty(key)));
    }
    //endregion

    //region Properties
    public String getClusterName() {
        return super.getString(CLUSTER_NAME, null);
    }

    public void setClusterName(String value) {
        super.setProperty(CLUSTER_NAME, value);
    }

    public String[] getClusterHosts() {
        return super.getStringArray(CLUSTER_HOSTS);
    }

    public void setClusterHosts(String[] value) {
        super.addProperty(CLUSTER_HOSTS, value);
    }

    public int getClusterPort() {
        return super.getInt(CLUSTER_PORT, 9300);
    }

    public void setClusterPort(int value) {
        super.addProperty(CLUSTER_PORT, value);
    }

    public ClientType getClientType() {
        return ClientType.valueOf(super.getString(CLIENT_TYPE, "transport"));
    }

    public void setClientType(ClientType value) {
        super.setProperty(CLIENT_TYPE, value.toString());
    }

    public boolean getClientTransportSniff() {
        return super.getBoolean(CLIENT_TRANSPORT_SNIFF, false);
    }

    public void setClientTransportSniff(boolean value){
        super.addProperty(CLIENT_TRANSPORT_SNIFF, value);
    }

    public boolean getClientTransportIgnoreClusterName() {
        return super.getBoolean(CLIENT_TRANSPORT_IGNORE_CLUSTER_NAME, false);
    }

    public void setClientTransportIgnoreClusterName(boolean value){
        super.addProperty(CLIENT_TRANSPORT_IGNORE_CLUSTER_NAME, value);
    }

    public String getClientTransportPingTimeout() {
        return super.getString(CLIENT_TRANSPORT_PING_TIMEOUT, "5s");
    }

    public void setClientTransportPingTimeout(String value) {
        super.addProperty(CLIENT_TRANSPORT_PING_TIMEOUT, value);
    }

    public String getClientTransportNodesSamplerInterval() {
        return super.getString(CLIENT_TRANSPORT_NODES_SAMPLER_INTERVAL, "5s");
    }

    public void setClientTransportNodesSamplerInterval(String value) {
        super.addProperty(CLIENT_TRANSPORT_NODES_SAMPLER_INTERVAL, value);
    }

    public String getElasticGraphIndexName() {
        return super.getString(ELASTIC_GRAPH_INDEX_NAME);
    }

    public void setElasticGraphIndexName(String value) {
        super.addProperty(ELASTIC_GRAPH_INDEX_NAME, value);
    }

    public UniGraphElementSchemaProviderFactory getElasticGraphSchemaProviderFactory() {
        return (UniGraphElementSchemaProviderFactory)super.getProperty(ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY);
    }

    public void setElasticGraphSchemaProviderFactory(UniGraphElementSchemaProviderFactory value) {
        super.addProperty(ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY, value);
    }

    public String getElasticGraphSchemaProviderFactoryClass() {
        return super.getString(ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY_CLASS);
    }

    public void setElasticGraphSchemaProviderFactoryClass(String value) {
        super.addProperty(ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY_CLASS, value);
    }

    public int getElasticGraphScrollTime() {
        return super.getInt(ELASTIC_GRAPH_SCROLL_TIME, 60000);
    }

    public int getElasticSearchScrollMaxOpen() {
        return super.getInt(ELASTIC_SEARCH_SCROLL_MAX_OPEN, 1000);
    }

    public void setElasticGraphScrollTime(int value) {
        super.addProperty(ELASTIC_GRAPH_SCROLL_TIME, value);
    }

    public int getElasticGraphScrollSize() {
        return super.getInt(ELASTIC_GRAPH_SCROLL_SIZE, 1000);
    }

    public void setElasticGraphScrollSize(int value) {
        super.addProperty(ELASTIC_GRAPH_SCROLL_SIZE, value);
    }

    public long getElasticGraphDefaultSearchSize() {
        return super.getLong(ELASTIC_GRAPH_DEFAULT_SEARCH_SIZE, 1000);
    }

    public void setElasticGraphDefaultSearchSize(long value) {
        super.addProperty(ELASTIC_GRAPH_DEFAULT_SEARCH_SIZE, value);
    }

    public long getElasticGraphMaxSearchSize() {
        return super.getLong(ELASTIC_GRAPH_MAX_SEARCH_SIZE, 1000);
    }

    public void setElasticGraphMaxSearchSize(long value) {
        super.addProperty(ELASTIC_GRAPH_MAX_SEARCH_SIZE, value);
    }

    public WriteMode getElasticGraphElementWriteMode() {
        return WriteMode.valueOf(super.getString(ELASTIC_GRAPH_ELEMENT_WRITE_MODE, "off"));
    }

    public void setElasticGraphElementWriteMode(WriteMode value) {
        super.setProperty(ELASTIC_GRAPH_ELEMENT_WRITE_MODE, value.toString());
    }

    public String getElasticGraphStrategyRegistrar() {
        return super.getString(ELASTIC_GRAPH_STRATEGY_REGISTRAR);
    }

    public void setElasticGraphStrategyRegistrar(String value) {
        super.addProperty(ELASTIC_GRAPH_STRATEGY_REGISTRAR, value);
    }

    public int getElasticGraphStrategyStepBulkSize() {
        return super.getInt(ELASTIC_GRAPH_STRATEGY_STEP_BULK_SIZE);
    }

    public void setElasticGraphStrategyStepBulkSize(int value) {
        super.setProperty(ELASTIC_GRAPH_STRATEGY_STEP_BULK_SIZE, value);
    }

    public String getClusterAddress() {
        return super.getString(CLUSTER_ADDRESS);
    }

    public void setClusterAddress(String value) {
        super.setProperty(CLUSTER_ADDRESS, value);
    }

    public int getElasticGraphAggregationsDefaultTermsSize() {
        return super.getInt(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SIZE);
    }

    public void setElasticGraphAggregationsDefaultTermsSize(int value) {
        super.setProperty(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SIZE, value);
    }

    public int getElasticGraphAggregationsDefaultTermsShardSize() {
        return super.getInt(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SHARD_SIZE);
    }

    public void setElasticGraphAggregationsDefaultTermsShardSize(int value) {
        super.setProperty(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SHARD_SIZE, value);
    }

    public String getElasticGraphAggregationsDefaultTermsExecutonHint() {
        return super.getString(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_EXECUTON_HINT);
    }

    public void setElasticGraphAggregationsDefaultTermsExecutonHint(String value) {
        super.setProperty(ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_EXECUTON_HINT, value);
    }

    public Map<String, String> getClusterProps() {
        return clusterProps;
    }

    public void setClusterProps(Set<Map.Entry<String, ConfigValue>> entrySet) {
        entrySet.forEach(entry->clusterProps.put("cluster."+entry.getKey(),entry.getValue().unwrapped().toString()));
    }

    //endregion

    //region Consts
    public static final String CLUSTER_PREFIX = "opensearch.cluster";
    public static final String CLUSTER_NAME = "opensearch.cluster.name";
    public static final String CLUSTER_HOSTS = "opensearch.cluster.hosts";
    public static final String CLUSTER_PORT = "opensearch.cluster.port";
    public static final String CLUSTER_ADDRESS = "opensearch.cluster.address";

    public static final String CLIENT_TYPE = "opensearch.client";
    public static final String CLIENT_TRANSPORT_SNIFF = "client.transport.sniff";
    public static final String CLIENT_TRANSPORT_IGNORE_CLUSTER_NAME = "client.transport.ignore_cluster_name";
    public static final String CLIENT_TRANSPORT_PING_TIMEOUT = "client.transport.ping_timeout";
    public static final String CLIENT_TRANSPORT_NODES_SAMPLER_INTERVAL = "client.transport.nodes_sampler_interval";

    public static final String ELASTIC_GRAPH_INDEX_NAME = "elastic.graph.index_name";
    public static final String ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY = "elastic.graph.schema_provider_factory";
    public static final String ELASTIC_GRAPH_SCHEMA_PROVIDER_FACTORY_CLASS = "elastic.graph.schema_provider_factory.class";
    public static final String ELASTIC_GRAPH_SCROLL_TIME = "elastic.graph.scroll_time";
    public static final String ELASTIC_SEARCH_SCROLL_MAX_OPEN = "opensearch.search.max_open_scroll_context";
    public static final String ELASTIC_GRAPH_SCROLL_SIZE = "elastic.graph.scroll_size";
    public static final String ELASTIC_GRAPH_DEFAULT_SEARCH_SIZE = "elastic.graph.default_search_size";
    public static final String ELASTIC_GRAPH_MAX_SEARCH_SIZE = "elastic.graph.max_search_size";
    public static final String ELASTIC_GRAPH_ELEMENT_WRITE_MODE = "elastic.graph.element.write_mode";

    public static final String ELASTIC_GRAPH_STRATEGY_REGISTRAR = "elastic.graph.strategy.registrar";
    public static final String ELASTIC_GRAPH_STRATEGY_STEP_BULK_SIZE = "elastic.graph.strategy.step_bulk_size";

    public static final String ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SIZE = "elastic.graph.aggregations.default_terms_size";
    public static final String ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_SHARD_SIZE = "elastic.graph.aggregations.default_terms_shard_size";
    public static final String ELASTIC_GRAPH_AGGREGATIONS_DEFAULT_TERMS_EXECUTON_HINT = "elastic.graph.aggregations.default_terms_execution_hint";

    //endregion

    public enum ClientType {
        TRANSPORT_CLIENT,
        NODE_CLIENT,
        NODE
    }

    public enum WriteMode {
        remote,
        local,
        localProperties,
        off
    }
}
