package org.opensearch.graph.model.query.properties;




import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by moti on 5/17/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BasePropGroup<T extends BaseProp, S extends BasePropGroup<T, S>> extends EBase {
    //region Constructors
    public BasePropGroup() {
        this(Collections.emptyList());
    }

    public BasePropGroup(int eNum) {
        this(eNum, Collections.emptyList());
    }

    public BasePropGroup(T...props) {
        this(Stream.of(props));
    }

    public BasePropGroup(Iterable<T> props) {
        this(0, props);
    }

    public BasePropGroup(int eNum, T...props) {
        this(eNum, Stream.of(props));
    }

    public BasePropGroup(int eNum, Iterable<T> props) {
        this(eNum, QuantType.all, props);
    }

    public BasePropGroup(int eNum, QuantType quantType, Iterable<T> props) {
        this(eNum, quantType, props, Collections.emptyList());
    }

    public BasePropGroup(int eNum, QuantType quantType, T ... props) {
        this(eNum, quantType, Arrays.asList(props), Collections.emptyList());
    }

    public BasePropGroup(int eNum, QuantType quantType, Iterable<T> props, Iterable<S> groups) {
        super(eNum);
        this.quantType = quantType;
        this.props = Stream.ofAll(props).toJavaList();
        this.groups = Stream.ofAll(groups).toJavaList();
    }
    //endregion

    //region Override Methods
    @Override
    @JsonIgnore
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        BasePropGroup that = (BasePropGroup) o;

        if (!this.quantType.equals(that.quantType)) {
            return false;
        }

        if (!(this.props != null ? this.props.equals(that.props) : that.props == null)) {
            return false;
        }

        if (!(this.groups != null ? this.groups.equals(that.groups) : that.groups == null)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.quantType.hashCode();
        result = 31 * result + (this.props != null ? this.props.hashCode() : 0);
        result = 31 * result + (this.groups != null ? this.groups.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public List<T> getProps() {
        return props;
    }

    public QuantType getQuantType() {
        return quantType;
    }

    public void setQuantType(QuantType quantType) {
        this.quantType = quantType;
    }

    public List<S> getGroups() {
        return groups;
    }
    //endregion

    //Region Fields
    protected List<T> props;
    protected QuantType quantType;
    protected List<S> groups;

    public boolean addIsNoneExist(T prop) {
        //add prop to list only when no similar prop exists
        if(this.props.stream().noneMatch(p->
                p.getpType().equals(prop.getpType())))
            return this.props.add(prop);
        return false;

    }

    /**
     * return true if created, false if replaced
     * @param prop
     * @return
     */
    public boolean addOrReplace(T prop) {
        //add prop to list only when no similar prop exists
        if(this.props.stream().noneMatch(p->
                p.getpType().equals(prop.getpType())))
            return this.props.add(prop);
        //replace existing
        T matchedProp = this.props.stream().findFirst().get();
        //replace fields
        matchedProp.setpType(prop.getpType());
        matchedProp.setF(prop.getF());
        matchedProp.setpTag(prop.getpTag());
        if(prop.isConstraint())
            matchedProp.setCon(prop.getCon());
        else
            matchedProp.setProj(prop.getProj());
        return false;

    }
    //endregion
}

