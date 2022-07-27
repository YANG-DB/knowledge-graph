package org.unipop.util;

/*-
 * #%L
 * unipop-core
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







import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalMetrics;
import org.unipop.query.UniQuery;
import org.unipop.query.controller.SimpleController;
import org.unipop.schema.element.ElementSchema;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MetricsRunner {
    private Optional<MutableMetrics> metrics;
    private MutableMetrics controllerMetrics;

    public MetricsRunner(SimpleController controller, UniQuery query,
                         Collection<ElementSchema> schemas) {
        if (query.getStepDescriptor() != null) {
            this.metrics = query.getStepDescriptor().getMetrics();
            this.controllerMetrics = new MutableMetrics(query.getStepDescriptor().getId() + controller.toString(), controller.toString());
        }
        else {
            this.metrics = Optional.empty();
            this.controllerMetrics = new MutableMetrics(controller.toString(), controller.toString());
        }
        metrics.ifPresent(metric -> metric.addNested(controllerMetrics));
        controllerMetrics.start();
        List<MutableMetrics> childMetrics = schemas.stream().map((schema) -> new MutableMetrics(controllerMetrics.getId() + schema.toString(), schema.toString())).collect(Collectors.toList());
        childMetrics.forEach(controllerMetrics::addNested);
    }

    @FunctionalInterface
    public interface FillChildren{
        void fillChildren(List<MutableMetrics> children);
    }

    public void stop(FillChildren fillChildren) {
        controllerMetrics.stop();
        if (metrics.isPresent()) {
            fillChildren.fillChildren(controllerMetrics.getNested().stream().map(m -> ((MutableMetrics) m)).collect(Collectors.toList()));
            controllerMetrics.setCount(TraversalMetrics.ELEMENT_COUNT_ID,
                    controllerMetrics.getNested().stream()
                            .mapToLong(n -> n.getCount(TraversalMetrics.ELEMENT_COUNT_ID)).sum());
        }
    }
}
