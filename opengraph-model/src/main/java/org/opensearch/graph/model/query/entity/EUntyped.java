package org.opensearch.graph.model.query.entity;





import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Untyped;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EUntyped extends EEntityBase implements Untyped {
    //region Constructors
    public EUntyped() {
        super();
        this.vTypes = new HashSet<>();
        this.nvTypes = new HashSet<>();
    }

    public EUntyped(int eNum, String eTag, int next, int b) {
        this(eNum, eTag, Collections.emptySet(), Collections.emptySet(), next, b);
    }

    public EUntyped(int eNum, String eTag, Set<String> vTypes, int next, int b) {
            this(eNum,eTag,vTypes,new HashSet<>(),next,b );
    }

    public EUntyped(int eNum, String eTag, Set<String> vTypes, Set<String> nvTypes, int next, int b) {
        super(eNum, eTag, next, b);
        this.vTypes = Stream.ofAll(vTypes).toJavaSet();
        this.nvTypes = Stream.ofAll(nvTypes).toJavaSet();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EUntyped eUntyped = (EUntyped) o;

        if (vTypes != null ? !vTypes.equals(eUntyped.vTypes) : eUntyped.vTypes != null) return false;
        return nvTypes != null ? nvTypes.equals(eUntyped.nvTypes) : eUntyped.nvTypes == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (vTypes != null ? vTypes.hashCode() : 0);
        result = 31 * result + (nvTypes != null ? nvTypes.hashCode() : 0);
        return result;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public EUntyped clone(int eNum) {
        final EUntyped clone = new EUntyped();
        clone.seteTag(geteTag());
        clone.seteNum(eNum);
        clone.nvTypes = new HashSet<>(nvTypes);
        clone.vTypes = new HashSet<>(vTypes);
        return clone;
    }

//endregion

    //region Properties
    @Override
    public Set<String> getvTypes() {
        return vTypes;
    }

    @Override
    public void setvTypes(Set<String> vTypes) {
        this.vTypes = vTypes;
    }

    @Override
    public Set<String> getNvTypes() {
        return nvTypes;
    }

    @Override
    public void setNvTypes(Set<String> nvTypes) {
        this.nvTypes = nvTypes;
    }
    //endregion

    //region Fields
    private Set<String> vTypes;
    private	Set<String> nvTypes;
    //endregion
}
