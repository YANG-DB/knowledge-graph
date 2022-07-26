package org.opensearch.graph.model.asgQuery;






import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsgCompositeQuery extends AsgQuery {
    public static final String INNER = "_Inner";
    private Set<AsgQuery> queryChain = new LinkedHashSet<>();

    public AsgCompositeQuery() {
    }

    public AsgCompositeQuery(AsgQuery asgQuery) {
        this.setName(asgQuery.getName());
        this.setOnt(asgQuery.getOnt());
        this.setOrigin(asgQuery.getOrigin());
        this.setParameters(asgQuery.getParameters());
        this.setStart(asgQuery.getStart());
        this.setElements(asgQuery.getElements());
    }

    public AsgCompositeQuery with(AsgQuery query) {
        queryChain.add(query);
        return this;
    }

    public List<AsgQuery> getQueryChain() {
        return new ArrayList<>(queryChain);
    }

    public static boolean isComposite(AsgQuery asgQuery) {
        return asgQuery instanceof AsgCompositeQuery;
    }

    public static boolean hasInnerQuery(AsgQuery asgQuery) {
        return isComposite(asgQuery) && !((AsgCompositeQuery) asgQuery).getQueryChain().isEmpty();
    }


}
