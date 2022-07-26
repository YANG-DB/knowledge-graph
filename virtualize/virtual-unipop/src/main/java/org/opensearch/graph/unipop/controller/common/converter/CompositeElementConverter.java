package org.opensearch.graph.unipop.controller.common.converter;





import javaslang.collection.Stream;

import java.util.Collections;

public class CompositeElementConverter<TElementSource, TElementDest> implements ElementConverter<TElementSource, TElementDest> {
    //region Constructors
    @SafeVarargs
    public CompositeElementConverter(ElementConverter<TElementSource, TElementDest>...elementConverters) {
        this(Stream.of(elementConverters));
    }

    public CompositeElementConverter(Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters) {
        this.elementConverters = Stream.ofAll(elementConverters).toJavaList();
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<TElementDest> convert(TElementSource tElementSource) {
        for(ElementConverter<TElementSource, TElementDest> elementConverter : this.elementConverters) {
            Iterable<TElementDest> elementsDest = elementConverter.convert(tElementSource);
            if (elementsDest != null) {
                return elementsDest;
            }
        }

        return Collections.emptyList();
    }
    //endregion

    //region Fields
    private Iterable<ElementConverter<TElementSource, TElementDest>> elementConverters;
    //endregion
}
