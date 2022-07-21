package org.opensearch.graph.unipop.process.traversal.traverser;


public interface StringOrdinalDictionary {
    String getString(byte ordinal);

    byte getOrdinal(String string);
    byte getOrCreateOrdinal(String string);
}
