package org.opensearch.graph.executor.ontology.schema;





import javaslang.Tuple2;

import java.util.List;

public interface OntologyIndexGenerator {
    List<Tuple2<String, Boolean>> generateMappings();

    List<Tuple2<Boolean, String>> createIndices();


}
