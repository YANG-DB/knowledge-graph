package org.opensearch.graph.model;




/**
 * Created by lior.perry on 5/8/2017.
 */
public interface Next<T> {
    T getNext();

    void setNext(T next);

    boolean hasNext();

}
