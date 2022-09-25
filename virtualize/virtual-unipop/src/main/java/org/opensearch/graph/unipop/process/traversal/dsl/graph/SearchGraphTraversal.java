package org.opensearch.graph.unipop.process.traversal.dsl.graph;

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





import org.opensearch.graph.unipop.process.traversal.traverser.ThinPathTraverserGeneratorFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.ChooseStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SearchGraphTraversal<S, E> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {
    //region Constructors
    public SearchGraphTraversal() {
    }

    public SearchGraphTraversal(GraphTraversalSource graphTraversalSource) {
        super(graphTraversalSource);

        this.getSideEffects().setSack(
                () -> (Map<String, Object>)new HashMap<String, Object>(),
                HashMap::new,
                (sack1, sack2) -> {
                    Map<String, Object> mergedSack = new HashMap<>(sack1);
                    mergedSack.putAll(sack2);
                    return mergedSack;
                });
    }

    public SearchGraphTraversal(Graph graph) {
        super(graph);
    }
    //endregion

    //region DefaultTraversal Implementation
    @Override
    public TraverserGenerator getTraverserGenerator() {
        if (this.generator == null) {
            this.generator = new ThinPathTraverserGeneratorFactory().getTraverserGenerator(Collections.emptySet());
        }
        return this.generator;
    }

    @Override
    public <E2> GraphTraversal<S, E2> optional(final Traversal<?, E2> optionalTraversal) {
        this.asAdmin().getBytecode().addStep("optional", new Object[]{optionalTraversal});
        return this.asAdmin().addStep(new ChooseStep(
                this.asAdmin(),
                (org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin)optionalTraversal,
                optionalTraversal.asAdmin().clone(),
                (org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin)__.start().identity()));
    }

//endregion

    //region GraphTraversal.Admin Implementation
    @Override
    public GraphTraversal.Admin<S, E> asAdmin() {
        return this;
    }

    @Override
    public GraphTraversal<S, E> iterate() {
        return (GraphTraversal<S, E>)super.iterate();
    }

    @Override
    public SearchGraphTraversal<S, E> clone() {
        return (SearchGraphTraversal) super.clone();
    }
    //endregion
}
