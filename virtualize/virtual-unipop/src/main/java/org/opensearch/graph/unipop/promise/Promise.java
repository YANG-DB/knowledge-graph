package org.opensearch.graph.unipop.promise;


public interface Promise {
    Object getId();

    static IdPromise as(String id) {
        return new IdPromise(id);
    }

    static IdPromise as(String id, String label) {
        return new IdPromise(id, label);
    }
}
