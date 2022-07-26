package org.opensearch.graph.model.query;







import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.Next;

/**
 * Created by lior.perry on 16/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Start extends EBase implements Next<Integer> {
    //region Constructors
    public Start() {}

    public Start(int eNum) {
        this(eNum,0);
    }


    public Start(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Start start = (Start) o;

        if (next != start.next) return false;
        return b == start.b;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
    //endregion

    //region Properties
    public Integer getNext() {
        return next;
    }

    @Override
    public boolean hasNext() {
        return next > -1;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion
}
