package org.opensearch.graph.executor.ontology.schema.load;



import java.io.IOException;

public class VoidGraphInitiator implements GraphInitiator {
    //region GraphDataLoader Implementation
    @Override
    public long init(String ontology)  {
        return 0;
    }

    @Override
    public long init()  {
        return 0;
    }


    @Override
    public long drop(String ontology)  {
        return 0;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }

    @Override
    public long createTemplate(String ontology, String schemaProvider) {
        return 0;
    }

    @Override
    public long createTemplate(String ontology) {
        return 0;
    }

    @Override
    public long createIndices(String ontology, String schemaProvider)  {
        return 0;
    }

    @Override
    public long createIndices(String ontology) {
        return 0;
    }
    //endregion
}
