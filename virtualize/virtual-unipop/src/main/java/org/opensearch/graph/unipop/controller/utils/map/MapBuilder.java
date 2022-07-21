package org.opensearch.graph.unipop.controller.utils.map;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MapBuilder<TKey, TValue> implements Supplier<Map<TKey, TValue>> {
    //region Constructors
    public MapBuilder() {
        this.map = new HashMap<>();
    }

    public MapBuilder(Map<TKey, TValue> map) {
        this();

        if (map != null) {
            this.map.putAll(map);
        }
    }
    //endregion

    //region Public Methods
    public MapBuilder<TKey, TValue> put(TKey key, TValue value) {
        this.map.put(key, value);
        return this;
    }

    public MapBuilder<TKey, TValue> putAll(Map<TKey, TValue> map) {
        this.map.putAll(map);
        return this;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Map<TKey, TValue> get() {
        return this.map;
    }
    //endregion

    //region Fields
    private Map<TKey, TValue> map;
    //endregion
}
