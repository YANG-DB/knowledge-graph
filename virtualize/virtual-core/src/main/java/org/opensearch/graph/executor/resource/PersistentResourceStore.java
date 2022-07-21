package org.opensearch.graph.executor.resource;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;
import org.opensearch.graph.dispatcher.cursor.CreateCursorRequestDeserializer;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryMetadata;
import org.opensearch.graph.model.transport.CreateQueryRequest;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.create.CreateIndexResponse;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.ActiveShardCount;
import org.opensearch.client.Client;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.rest.RestStatus;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.StorageType._stored;
import static org.opensearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.opensearch.index.query.QueryBuilders.matchAllQuery;

public class PersistentResourceStore implements ResourceStore {

    public static final String SYSTEM = "fuse_system";
    public static final String PROJECTION = "projection";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TTL = "ttl";
    public static final String CREATION_TIME = "creationTime";
    public static final String QUERY = "query";
    public static final String ASG = "asg";
    public static final String REQUEST = "request";
    public static final String TYPE = "type";
    public static final String RESOURCE = "resource";
    private Client client;
    private ObjectMapper mapper;

    @Inject
    public PersistentResourceStore(Provider<Client> client, ObjectMapper mapper, Set<CompositeCursorFactory.Binding> cursorBindings) {
        this.client = client.get();
        this.mapper = mapper;
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class, new CreateCursorRequestDeserializer(cursorBindings));
        mapper.registerModules(module);
    }

    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return getQueryResources().stream()
                .filter(p->predicate.test(p.getQueryMetadata().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<QueryResource> getQueryResources() {
        while (true) {
            try {
                final SearchRequestBuilder search = client.prepareSearch(SYSTEM);
                final SearchResponse response = search.setQuery(matchAllQuery()).get();
                return Arrays.asList(response.getHits().getHits()).stream()
                        .map(hit -> {
                            try {
                                return buildQueryResource(hit.getId(),hit.getSourceAsMap());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (IndexNotFoundException e) {
                this.client.admin().indices()
                        .create(new CreateIndexRequest()
                                .waitForActiveShards(ActiveShardCount.ALL)
                                .index(SYSTEM)).actionGet();

            }
        }
    }

    private QueryResource buildQueryResource(String id, Map hit) throws IOException {
        final String name = hit.get(NAME).toString();
        final String ttl = hit.get(TTL).toString();
        final String creationTime = hit.get(CREATION_TIME).toString();
        final Query query = mapper.readValue(hit.getOrDefault(QUERY, "{}").toString(), Query.class);
        final AsgQuery asgQuery = mapper.readValue(hit.getOrDefault(ASG, "{}").toString(), AsgQuery.class);
        final CreateQueryRequest request = mapper.readValue(hit.getOrDefault(REQUEST,"{}").toString(),CreateQueryRequest.class);
        final QueryMetadata queryMetadata = new QueryMetadata(_stored, id, name, request.isSearchPlan(), Long.valueOf(creationTime), Long.valueOf(ttl));
        return new QueryResource(request, query, asgQuery, queryMetadata, PlanWithCost.EMPTY_PLAN);
    }

    @Override
    public Optional<QueryResource> getQueryResource(String queryId) {
        try {
            final GetResponse response = client.prepareGet(SYSTEM, RESOURCE, queryId).get();
            if (response.isExists())
                return Optional.of(buildQueryResource(queryId, response.getSource()));
        } catch (IndexNotFoundException e) {
            final CreateIndexResponse response = this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .waitForActiveShards(ActiveShardCount.ALL)
                            .index(SYSTEM)).actionGet();
            if(response.isAcknowledged()) {
                if (client.prepareGet(SYSTEM, RESOURCE, queryId).get().isExists()) {
                    try {
                        return Optional.of(buildQueryResource(queryId, client.prepareGet(SYSTEM, RESOURCE, queryId).get().getSource()));
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        try {
            IndexResponse response = client.prepareIndex(SYSTEM, RESOURCE, queryResource.getQueryMetadata().getId())
                    .setSource(jsonBuilder()
                            .startObject()
                            .field(NAME, queryResource.getQueryMetadata().getName())
                            .field(TTL, queryResource.getQueryMetadata().getTtl())
                            .field(CREATION_TIME, queryResource.getQueryMetadata().getCreationTime())
                            .field(REQUEST, mapper.writeValueAsString(queryResource.getRequest()))
                            .field(QUERY, mapper.writeValueAsString(queryResource.getQuery()))
                            .field(ASG, mapper.writeValueAsString(queryResource.getAsgQuery()))
                            .endObject()
                    ).execute().actionGet();
            return response.status() == RestStatus.CREATED || response.status() == RestStatus.OK;
        } catch (IndexNotFoundException e) {
            this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .waitForActiveShards(ActiveShardCount.ALL)
                            .index(SYSTEM)).actionGet();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        final DeleteResponse response = client.prepareDelete(SYSTEM, RESOURCE, queryId).get();
        return response.status() == RestStatus.OK;
    }

    /**
     * ----------NOT-IMPLEMENTED-----NOT-IMPLEMENTED-----NOT-IMPLEMENTED--------------------------------------------
     **/
    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        return Optional.empty();
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        return Optional.empty();
    }

    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        return false;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        return false;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        return false;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        return false;
    }

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return type.equals(_stored);
    }
}
