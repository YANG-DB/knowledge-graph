package org.unipop.process.traverser;

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








import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.javatuples.Pair;

import java.util.NoSuchElementException;

public class UniGraphTraverserStep<S> extends AbstractStep<S, Traverser<S>> {
    public UniGraphTraverserStep(Traversal.Admin traversal) {
        super(traversal);
    }

    @Override
    protected Traverser.Admin<Traverser<S>> processNextStart() throws NoSuchElementException {
        if (starts.hasNext()) {
            Traverser.Admin<S> next = starts.next();
            return next.split(next, this);
        }
        throw FastNoSuchElementException.instance();
    }
}
