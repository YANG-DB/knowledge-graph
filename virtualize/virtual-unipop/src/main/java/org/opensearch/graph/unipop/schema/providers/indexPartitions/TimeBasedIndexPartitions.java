package org.opensearch.graph.unipop.schema.providers.indexPartitions;

/*-
 * #%L
 * virtual-unipop
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

import javaslang.collection.Stream;
import org.opensearch.graph.model.schema.Props;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TimeBasedIndexPartitions implements TimeSeriesIndexPartitions {
    private Props props;
    private SimpleDateFormat dateFormat;

    public TimeBasedIndexPartitions(Props props) {
        this.props = props;
        this.dateFormat = new SimpleDateFormat(getDateFormat());
    }


    @Override
    public String getDateFormat() {
        return props.getDateFormat();
    }

    @Override
    public String getIndexPrefix() {
        return props.getPrefix();
    }

    @Override
    public String getIndexFormat() {
        return props.getIndexFormat();
    }

    @Override
    public String getTimeField() {
        return props.getPartitionField();
    }

    @Override
    public String getIndexName(Date date) {
        String format = String.format(getIndexFormat(), dateFormat.format(date));
        List<String> indices = Stream.ofAll(getPartitions())
                .flatMap(Partition::getIndices)
                .filter(index -> index.equals(format))
                .toJavaList();

        return indices.isEmpty() ? null : indices.get(0);
    }

    @Override
    public Optional<String> getPartitionField() {
        return Optional.of(getTimeField());
    }

    @Override
    public Iterable<Partition> getPartitions() {
        return Collections.singletonList(() -> Stream.ofAll(props.getValues())
                .map(p -> String.format(getIndexFormat(), p))
                .distinct().sorted()
                .toJavaList());
    }
}
