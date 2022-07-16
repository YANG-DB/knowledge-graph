package org.opensearch.graph.model.query;

/*-
 * #%L
 * opengraph-model
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*-
 *
 * QueryMetadata.java - opengraph-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.opensearch.graph.model.transport.CreateQueryRequestMetadata;

import java.util.UUID;


/**
 * Created by lior.perry on 21/02/2017.
 */
public final class QueryMetadata {

    public interface QueryMetadataAble {
        QueryMetadata getQueryMetadata();
    }

    public static QueryMetadata random(String name, boolean searchPlan) {
        return new QueryMetadata(CreateQueryRequestMetadata.StorageType._volatile, UUID.randomUUID().toString(),name,searchPlan,System.currentTimeMillis(),10000);
    }

    //region Properties
    public QueryMetadata(CreateQueryRequestMetadata.StorageType storageType, String id, String name, boolean searchPlan , long creationTime, long ttl) {
        this(CreateQueryRequestMetadata.QueryType.concrete, storageType, id, name, searchPlan, creationTime, ttl);
    }

    public QueryMetadata(CreateQueryRequestMetadata.QueryType type, CreateQueryRequestMetadata.StorageType storageType, String id, String name, boolean searchPlan , long creationTime, long ttl) {
        this.storageType = storageType;
        this.id = id;
        this.name = name;
        this.searchPlan = searchPlan;
        this.creationTime = creationTime;
        this.ttl = ttl;
        this.type = type;
    }

    public boolean isSearchPlan() {
        return searchPlan;
    }

    public CreateQueryRequestMetadata.StorageType getStorageType() {
        return storageType;
    }

    public String getId() {
        return id;
    }

    public CreateQueryRequestMetadata.QueryType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getTtl() {
        return ttl;
    }

    public void setSearchPlan(boolean searchPlan) {
        this.searchPlan = searchPlan;
    }

    public void setStorageType(CreateQueryRequestMetadata.StorageType storageType) {
        this.storageType = storageType;
    }

    public void setType(CreateQueryRequestMetadata.QueryType type) {
        this.type = type;
    }

    @Override
    public QueryMetadata clone() {
        return new QueryMetadata(storageType,id,name,searchPlan,creationTime,ttl);
    }

    //endregion

    //region Fields
    private long creationTime;
    private long ttl;
    private String id;
    private String name;
    private boolean searchPlan = true;
    private CreateQueryRequestMetadata.StorageType storageType;
    private CreateQueryRequestMetadata.QueryType type;
    //endregion

}
