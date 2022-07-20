package org.opensearch.graph.test.framework.index;

/*-
 * #%L
 * test-framework
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

import org.apache.http.HttpHost;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.MainResponse;
import org.opensearch.common.settings.Settings;

import java.util.Optional;

/**
 * Created by roman.margolis on 01/01/2018.
 */
public class GlobalSearchEmbeddedNode {
    private static SearchEmbeddedNode instance;
    private static String nodeName;

    public static Optional<MainResponse> isRunningLocally() {
        try {
            final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return Optional.of(response);
        } catch (Throwable err) {
            //couldnt connect -
            return Optional.empty();
        }
    }

    public static SearchEmbeddedNode getInstance() throws Exception {
        return getInstance("fuse.test_elastic");
    }

    public static SearchEmbeddedNode getInstance(Settings setting) throws Exception {
        synchronized (SearchEmbeddedNode.class) {
            if (instance == null) {
                instance = new SearchEmbeddedNode(setting, "target/es", 9200, 9300, nodeName);
                System.out.println("Starting embedded Elasticsearch Node " + nodeName);
            } else if (!GlobalSearchEmbeddedNode.nodeName.equals(nodeName)) {
                close();
                instance = new SearchEmbeddedNode(setting, "target/es", 9200, 9300, nodeName);
            }
            GlobalSearchEmbeddedNode.nodeName = nodeName;
            return instance;
        }

    }

    public static SearchEmbeddedNode getInstance(String nodeName) throws Exception {
        synchronized (SearchEmbeddedNode.class) {
            if (instance == null) {
                instance = new SearchEmbeddedNode(Settings.EMPTY, "target/es", 9200, 9300, nodeName);
                System.out.println("Starting embedded Elasticsearch Node " + nodeName);
            } else if (!GlobalSearchEmbeddedNode.nodeName.equals(nodeName)) {
                close();
                instance = new SearchEmbeddedNode(Settings.EMPTY, "target/es", 9200, 9300, nodeName);
            }
            GlobalSearchEmbeddedNode.nodeName = nodeName;
            return instance;
        }
    }


    public static void close() {
        synchronized (SearchEmbeddedNode.class) {
            if (instance != null) {
                try {
                    instance.close();
                    instance = null;
                    System.out.println("Stopping embedded Elasticsearch Node " + nodeName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
