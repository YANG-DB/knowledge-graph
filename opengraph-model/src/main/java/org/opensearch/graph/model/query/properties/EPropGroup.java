package org.opensearch.graph.model.query.properties;







import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by benishue on 25-Apr-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EPropGroup extends BasePropGroup<EProp, EPropGroup> {
    //region Constructors
    public EPropGroup() {
        super(Collections.emptyList());
    }

    public EPropGroup(int eNum) {
        super(eNum);
    }

    public EPropGroup(EProp... props) {
        super(props);
    }

    public EPropGroup(Iterable<EProp> props) {
        super(0, props);
    }

    public EPropGroup(List<EProp> props) {
        super(0, props);
    }

    public EPropGroup(int eNum, EProp... props) {
        super(eNum, props);
    }

    public EPropGroup(int eNum, Iterable<EProp> props) {
        super(eNum, props);
    }

    public EPropGroup(int eNum, QuantType quantType, Iterable<EProp> props) {
        super(eNum, quantType, props, Collections.emptyList());
    }

    public EPropGroup(int eNum, QuantType quantType, EProp... props) {
        super(eNum, quantType, props);
    }

    public EPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups) {
        super(eNum, quantType, props, groups);
    }
    //endregion

    //region Public Methods
    @JsonIgnore
    public List<EProp> findAll(Predicate<EProp> propPredicate) {
        return this.findAll(propPredicate, this);
    }

    @JsonIgnore
    public void consumeAll(Predicate<EProp> propPredicate, BiConsumer<EPropGroup, EProp> consumer) {
        this.consumeAll(propPredicate, this, consumer);
    }
    //endregion

    //region Override Methods
    @Override
    @JsonIgnore
    public EPropGroup clone() {
        return clone(geteNum());
    }

    @Override
    @JsonIgnore
    public EPropGroup clone(int eNum) {
        AtomicInteger propsNum = new AtomicInteger(eNum);
        AtomicInteger groupsNum = new AtomicInteger(eNum);
        return new EPropGroup(eNum,
        this.getQuantType(),
                this.getProps().stream().map(p->p.clone(propsNum.incrementAndGet())).collect(Collectors.toList()),
                this.getGroups().stream().map(p->p.clone(groupsNum.incrementAndGet())).collect(Collectors.toList()));
    }
    //endregion

    //region Static
    public static EPropGroup of(int eNum, EProp... props) {
        return new EPropGroup(eNum, props);
    }

    public static EPropGroup of(int eNum, QuantType quantType, EProp... props) {
        return new EPropGroup(eNum, quantType, Stream.of(props));
    }

    public static EPropGroup of(int eNum, EPropGroup... groups) {
        return new EPropGroup(eNum, QuantType.all, Collections.emptyList(), Stream.of(groups));
    }

    public static EPropGroup of(int eNum, QuantType quantType, EPropGroup... groups) {
        return new EPropGroup(eNum, quantType, Stream.empty(), Stream.of(groups));
    }

    public static EPropGroup of(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups) {
        return new EPropGroup(eNum, quantType, props, groups);
    }

    public static List<EPropGroup> findInGroupRecursive(EPropGroup group, Predicate<EPropGroup> predicate) {
        ArrayList<EPropGroup> groups = new ArrayList<>();
        findInGroupRecursive(groups, group, predicate);
        return groups;
    }

    /**
     * finds predicate for group & inner groups and adds all groups that apply the predicate & all groups in path (parent groups) to the root
     *
     * @param groups
     * @param group
     * @param predicate
     * @return
     */
    private static boolean findInGroupRecursive(List<EPropGroup> groups, EPropGroup group, Predicate<EPropGroup> predicate) {
        boolean result = false;
        Option<EPropGroup> ePropGroups = Stream.ofAll(group.getGroups()).find(predicate);
        if (!ePropGroups.isEmpty() && !groups.contains(ePropGroups.get())) {
            groups.add(ePropGroups.get());
            result = true;
        }
        Iterator<EPropGroup> iterator = group.getGroups().iterator();
        while(iterator.hasNext()) {
            if (findInGroupRecursive(groups, iterator.next(), predicate) || result) {
                groups.add(group);
            }
        }
        return result;
    }

    private List<EProp> findAll(Predicate<EProp> propPredicate, EPropGroup group) {
        return Stream.ofAll(group.getProps())
                .appendAll(Stream.ofAll(group.getGroups()).flatMap(childGroup -> findAll(propPredicate, childGroup)))
                .filter(propPredicate)
                .toJavaList();
    }

    private void consumeAll(Predicate<EProp> propPredicate, EPropGroup group, BiConsumer<EPropGroup, EProp> consumer) {
        Stream.ofAll(group.getProps()).filter(propPredicate).toJavaList().forEach(eprop -> consumer.accept(group, eprop));
        Stream.ofAll(group.getGroups()).forEach(childGroup -> consumeAll(propPredicate, childGroup, consumer));
    }
    //endregion
}
