package org.opensearch.graph.model.query.entity;





import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.Next;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.query.EBase;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EAgg extends EBase implements Next<Integer>, Tagged {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

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
    private	String eTag;
    private	String fName;
    private	String eName;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int	next;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EAgg eAgg = (EAgg) o;

        if (next != eAgg.next) return false;
        if (!eTag.equals(eAgg.eTag)) return false;
        if (!fName.equals(eAgg.fName)) return false;
        return eName.equals(eAgg.eName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eTag.hashCode();
        result = 31 * result + fName.hashCode();
        result = 31 * result + eName.hashCode();
        result = 31 * result + next;
        return result;
    }
}
