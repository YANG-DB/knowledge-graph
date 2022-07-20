package org.opensearch.graph.executor.ontology.schema;

/*-
 * #%L
 * virtual-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.google.common.collect.Lists;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;

import java.util.List;

public interface PartitionResolver {

    IndexPartitions getPartition(String type);

    List<IndexPartitions.Partition> getPartitions(String type);


    class StaticPartitionResolver implements PartitionResolver {
        private StaticIndexPartitions indexPartitions;

        public StaticPartitionResolver(String... indices) {
            this.indexPartitions = new StaticIndexPartitions(indices);
        }

        @Override
        public IndexPartitions getPartition(String type) {
            return indexPartitions;
        }

        @Override
        public List<IndexPartitions.Partition> getPartitions(String type) {
            return Lists.newArrayList(indexPartitions.getPartitions());
        }
    }
}
