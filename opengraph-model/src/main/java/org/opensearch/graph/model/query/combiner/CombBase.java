package org.opensearch.graph.model.query.combiner;







import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.Next;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CombBase extends EBase implements Next<Integer> {
    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return next > -1;
    }

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion

}
