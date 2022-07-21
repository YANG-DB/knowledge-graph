
package org.unipop.structure;




/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class EmptyUniVertexProperty<V> implements VertexProperty<V> {

    @Override
    public Vertex element() {
        throw Property.Exceptions.propertyDoesNotExist();
    }

    @Override
    public Object id() {
        throw Property.Exceptions.propertyDoesNotExist();
    }

    @Override
    public Graph graph() {
        throw Property.Exceptions.propertyDoesNotExist();
    }

    @Override
    public <U> Property<U> property(String key) {
        return Property.<U>empty();
    }

    @Override
    public <U> Property<U> property(String key, U value) {
        return Property.<U>empty();
    }

    @Override
    public abstract String key();

    @Override
    public abstract V value();

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public void remove() {}

    @Override
    public String toString() {
        return StringFactory.propertyString(this);
    }

    @Override
    public <U> Iterator<Property<U>> properties(String... propertyKeys) {
        return Collections.emptyIterator();
    }
}
