package org.opensearch.graph.model.execution.plan.descriptors;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.query.Query;

public class JacksonQueryDescriptor implements Descriptor<Query> {
    //region Constructors
    public JacksonQueryDescriptor() {
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(Query item) {
        try {
            return this.mapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Fields
    private ObjectMapper mapper;
    //endregion
}
