package org.opensearch.graph.model.log;

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
 * TraceCompositeAble.java - opengraph-model - yangdb - 2,016
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

import javaslang.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 5/9/2017.
 */
public class TraceCompositeAble<T> extends TraceAble<T> implements TraceComposite<T>{
    private List<Trace<T>> traces;

    public TraceCompositeAble(String who)    {
        super(who);
        traces = new ArrayList<>();
    }

    @Override
    public List<Tuple2<String,T>> getLogs(Level level) {
        List<Tuple2<String, T>> logs = super.getLogs(level);
        List<Tuple2<String, T>> collect = traces.stream().map(t -> t.getLogs(level))
                .filter(log -> !log.isEmpty())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        logs.addAll(collect);
        return logs;
    }

    @Override
    public void with(Trace<T> trace) {
        traces.add(trace);
    }
}
