package org.opensearch.graph.unipop.process.traversal.strategy.decoration;

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





import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.RequirementsStep;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.AbstractTraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ForceRequirementsStrategy extends AbstractTraversalStrategy<TraversalStrategy.DecorationStrategy> implements TraversalStrategy.DecorationStrategy {

    private final Set<TraverserRequirement> requirements = new HashSet<>();

    private ForceRequirementsStrategy() {
    }

    @Override
    public void apply(final Traversal.Admin<?, ?> traversal) {
        traversal.addStep(new RequirementsStep<>(traversal, this.requirements));
    }

    public static void addRequirements(final TraversalStrategies traversalStrategies, final TraverserRequirement... requirements) {
        ForceRequirementsStrategy strategy = (ForceRequirementsStrategy) traversalStrategies.toList().stream().filter(s -> s instanceof ForceRequirementsStrategy).findAny().orElse(null);
        if (null == strategy) {
            strategy = new ForceRequirementsStrategy();
            traversalStrategies.addStrategies(strategy);
        } else {
            final ForceRequirementsStrategy cloneStrategy = new ForceRequirementsStrategy();
            cloneStrategy.requirements.addAll(strategy.requirements);
            strategy = cloneStrategy;
            traversalStrategies.addStrategies(strategy);
        }
        Collections.addAll(strategy.requirements, requirements);
    }
}
