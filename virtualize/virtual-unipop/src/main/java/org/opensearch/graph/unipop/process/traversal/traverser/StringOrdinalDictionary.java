package org.opensearch.graph.unipop.process.traversal.traverser;


/**
 * Created by Roman on 1/27/2018.
 */
public interface StringOrdinalDictionary {
    String getString(byte ordinal);

    byte getOrdinal(String string);
    byte getOrCreateOrdinal(String string);
}
