package org.unipop.process;

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





import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.util.iterator.EmptyIterator;
//import org.unipop.process.traversal.QueueExpandableStepIterator;
import org.unipop.common.valueSuppliers.CompiledSupplierFactory;
import org.unipop.process.bulk.BulkIterator;
import org.unipop.common.valueSuppliers.FixedValueSupplierFactory;
import org.unipop.common.valueSuppliers.LinearDecayingValueSupplierFactory;
import org.unipop.structure.UniGraph;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static org.unipop.process.Profiler.PROFILER;

public abstract class UniBulkStep<S, E> extends AbstractStep<S, E> {
    private static Supplier<Supplier<Integer>> cachedBulkSizeSupplierFactory = null;

    //region Constructors
    public UniBulkStep(Traversal.Admin traversal, UniGraph graph) {
        super(traversal);

        int maxBulkSize = graph.configuration().getInt("bulk.max", 100);
        int minBulkSize = graph.configuration().getInt("bulk.min", maxBulkSize);
        long decayInterval = graph.configuration().getLong("bulk.decayInterval", 200L);

        if (cachedBulkSizeSupplierFactory == null) {
            if (maxBulkSize == minBulkSize) {
                cachedBulkSizeSupplierFactory = new FixedValueSupplierFactory(maxBulkSize);
            } else {
                cachedBulkSizeSupplierFactory = new CompiledSupplierFactory(
                        new LinearDecayingValueSupplierFactory(maxBulkSize, minBulkSize, decayInterval),
                        decayInterval,
                        100,
                        CompiledSupplierFactory.ValueAggMethod.max);
            }
        }

        this.bulkSizeSupplierFactory = cachedBulkSizeSupplierFactory;

        this.results = EmptyIterator.instance();

        this.profiler = this.traversal.getSideEffects().exists(PROFILER) ?
                this.traversal.getSideEffects().get(PROFILER) :
                Profiler.Noop.instance;
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
        if (this.results.equals(EmptyIterator.instance())) {
            this.results = process();
        }

        return results.next();
    }

    @Override
    public void reset() {
        super.reset();
        this.results = EmptyIterator.instance();
    }
    //endregion

    //region Abstract Methods
    protected abstract Iterator<Traverser.Admin<E>> process(List<Traverser.Admin<S>> traversers);
    //endregion

    //region Private Methods
    protected Iterator<Traverser.Admin<E>> process() {
        return Stream.ofAll(() -> new BulkIterator<>(this.starts, this.bulkSizeSupplierFactory))
                .flatMap(bulk -> Stream.ofAll(() -> process(bulk))).iterator();
    }
    //endregion

    //region Fields
    protected Supplier<Supplier<Integer>> bulkSizeSupplierFactory;
    protected Iterator<Traverser.Admin<E>> results = null;

    protected Profiler profiler;
    //endregion
}
