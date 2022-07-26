package org.openserach.graph.asg.strategy.type;






import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.Range;
import org.opensearch.graph.model.Tagged;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.RelPattern;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.EndPattern;
import org.opensearch.graph.model.query.entity.TypedEndPattern;
import org.opensearch.graph.model.query.entity.UnTypedEndPattern;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.BasePropGroup;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.Stream.concat;

public class RelationPatternRangeAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AtomicInteger counter = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());
        Stream.ofAll(AsgQueryUtil.elements(query, RelPattern.class))
                .forEach(relPattern -> {
                    //get end Pattern entity - should exist according to the validation
                    final Optional<AsgEBase<EBase>> endPattern = Optional.of(
                            AsgQueryUtil.nextDescendant(relPattern, UnTypedEndPattern.class)
                                    .orElseGet(()->AsgQueryUtil.nextDescendant(relPattern, TypedEndPattern.class).get()));

                    //get parent element of type Entity
                    Optional<AsgEBase<EEntityBase>> parent = AsgQueryUtil.ancestor(relPattern, EEntityBase.class);
                    //if not present something is wrong with the query  - the validator should inform this
                    if (parent.isPresent() && endPattern.isPresent()) {
                        //check is there is a Or type quant in between rel & parent
                        List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(parent.get(), relPattern);
                        Optional<AsgEBase<Quant1>> quant = path.stream().filter(p -> p.geteBase() instanceof QuantBase).map(q -> (AsgEBase<Quant1>) q).findFirst();

                        //no quant present - create one for the inner or
                        if (!quant.isPresent()) {
                            // add quant of 'Or' type after the rel
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(counter, query, quantAsg, relPattern, endPattern.get());
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                            parent.get().addNext(quantAsg);
                        } else if (quant.get().geteBase().getqType().equals(QuantType.all)) {
                            // quant of type all exist -> replace with an OR quant and add condition to all union parts
                            Quant1 newQuant = new Quant1(counter.incrementAndGet(), QuantType.some);
                            AsgEBase<Quant1> quantAsg = new AsgEBase<>(newQuant);
                            addRelPattern(counter, query, quantAsg, relPattern, endPattern.get());
                            //remove pattern
                            quant.get().removeNextChild(relPattern);
                            //add or quant to containing And quant
                            quant.get().addNext(quantAsg);
                        } else {
                            // quant of type some exist -> add the inner or patterns after the quant
                            addRelPattern(counter, query, quant.get(), relPattern, endPattern.get());
                            //remove pattern
                            parent.get().removeNextChild(relPattern);
                        }
                    }
                });

        //replace all EndPatterns with its internal real entity
        List<AsgEBase<? extends EBase>> collect = concat(
                AsgQueryUtil.elements(query, UnTypedEndPattern.class).stream(),
                AsgQueryUtil.elements(query, TypedEndPattern.class).stream())
                .collect(Collectors.toList());
        //replace end pattern
        collect.forEach(p -> replaceEndPattern(counter, (AsgEBase<EBase>) p));

    }

    /**
     * replace end pattern with the actual type - including its possible filters
     *
     * @param counter
     * @param p
     */
    public void replaceEndPattern(AtomicInteger counter, AsgEBase<EBase> p) {
        if (!((EndPattern) p.geteBase()).getFilter().isEmpty()) {
            //if end pattern is last in query - add quant for the purpose of the end-pattern filters
            if (!p.hasNext()) {
                addEndPatternFilters(counter, p);
            } else {
                //if end pattern is not last in query - search for a quant right after
                Optional<AsgEBase<QuantBase>> quantOp = AsgQueryUtil.nextAdjacentDescendant(p, QuantBase.class, 1);
                // if quant not present - add one and chain all childs to that quant
                if (!quantOp.isPresent()) {
                    AsgEBase<QuantBase> quantAsg = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all));
                    AsgQueryUtil.replaceParents(quantAsg, p);
                    p.addNext(quantAsg);
                }
                // quant present - add end-pattern filter to quant
                quantOp = AsgQueryUtil.nextAdjacentDescendant(p, QuantBase.class, 1);
                EPropGroup ePropGroup = new EPropGroup(((EndPattern) p.geteBase()).getFilter()).clone(counter.incrementAndGet());
                quantOp.get().addNext(new AsgEBase<>(ePropGroup));
            }
        }
        //change endPattern type to actual type
        p.seteBase(((EndPattern<EBase>) p.geteBase()).getEndEntity());
    }

    public QuantBase addEndPatternFilters(AtomicInteger counter, AsgEBase<EBase> p) {
        QuantBase newQuant = new Quant1(counter.incrementAndGet(), QuantType.all);
        AsgEBase<QuantBase> quantAsg = new AsgEBase<>(newQuant);
        EPropGroup ePropGroup = new EPropGroup(((EndPattern) p.geteBase()).getFilter()).clone(counter.incrementAndGet());
        quantAsg.addNext(new AsgEBase<>(ePropGroup));
        p.addNext(quantAsg);
        return newQuant;
    }

    /**
     * add a number of steps according to the given range in the rel pattern
     *
     * @param query
     * @param quantAsg
     * @param relPattern
     */
    private void addRelPattern(AtomicInteger counter, AsgQuery query, AsgEBase<Quant1> quantAsg, AsgEBase<RelPattern> relPattern, AsgEBase<EBase> endPattern) {
        Range range = relPattern.geteBase().getLength();
        //duplicate the rel pattern according to the range, range should already be validated by the validator
        LongStream.rangeClosed(range.getLower(), range.getUpper())
                //this is the Root some quant all pattern premutations will be added to...
                .forEach(value -> {
                    //if value == 0  remove the RelPattern entirely
                    if (value == 0) {
                        //take the path after the end pattern section (if exists) & add it as no hop pattern to the Quant
                        if (endPattern.hasNext()) {
                            final AsgEBase<? extends EBase> nextAfterEndPattern = endPattern.getNext().get(0);
                            final AsgEBase<? extends EBase> afterEndPattern = AsgQueryUtil.deepCloneWithEnums(counter, nextAfterEndPattern, e -> true, e -> true);
                            quantAsg.addNext(afterEndPattern);
                        }
                    } else {
                        final AsgEBase<? extends EBase> relConcretePattern = addPath(counter, value, relPattern, endPattern);
                        //add the path after the end pattern section
                        if (endPattern.hasNext()) {
                            final AsgEBase<? extends EBase> nextAfterEndPattern = endPattern.getNext().get(0);
                            final AsgEBase<? extends EBase> afterEndPattern = AsgQueryUtil.deepCloneWithEnums(counter, nextAfterEndPattern, e -> true, e -> true);
                            //get last Descendant of same type of end pattern
                            final List<AsgEBase<EBase>> endElements =
                                    concat(AsgQueryUtil.nextDescendants(relConcretePattern, UnTypedEndPattern.class).stream(),
                                            AsgQueryUtil.nextDescendants(relConcretePattern, TypedEndPattern.class).stream())
                                            .collect(Collectors.toList());
                            endElements.get(endElements.size() - 1).addNext(afterEndPattern);
                        }
                        quantAsg.addNext(relConcretePattern);
                    }
                });
    }

    /**
     * rel pattern premutation generator
     *
     * @param value
     * @param relPattern
     * @param endPattern
     * @return
     */
    private AsgEBase<? extends EBase> addPath(AtomicInteger counter, long value, AsgEBase<RelPattern> relPattern, AsgEBase<EBase> endPattern) {
        final AtomicReference<AsgEBase<? extends EBase>> current = new AtomicReference<>();
        LongStream.rangeClosed(1, value)
                .forEach(step -> {
                    AsgEBase<? extends EBase> node = buildStep(counter, relPattern, endPattern);
                    if (current.get() == null) {
                        current.set(node);
                    } else {
                        //the build step returns a cloned pattern  of [rel:Rel]-....->(endPattern:Entity)->...
                        //
                        if (!(current.get().geteBase() instanceof QuantBase)) {
                            final AsgEBase<Quant1> quant = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all));
                            AsgQueryUtil.addAsNext(quant, current.get());
                            current.set(quant);
                        } else {
                            //knowing that the rel pattern has a shape of a line not a tree get the last Descendant
                            current.set(
                                    AsgQueryUtil.nextDescendant(current.get(), UnTypedEndPattern.class)
                                            .orElseGet(()->AsgQueryUtil.nextDescendant(current.get(), TypedEndPattern.class).get()));
                        }
                        current.get().addNext(AsgQueryUtil.ancestorRoot(node).get());
                        current.set(node);
                    }
                });
        //get first node in the path to add to the containing quant
        return AsgQueryUtil.ancestorRoot(current.get()).get();
    }


    /**
     * build a new complete rel->pattern step cloned from existing step
     *
     * @param relPattern
     * @param endPattern
     * @return
     */
    private AsgEBase<? extends EBase> buildStep(AtomicInteger counter, AsgEBase<RelPattern> relPattern, AsgEBase<EBase> endPattern) {

        RelPattern pattern = relPattern.geteBase();
        List<AsgEBase<? extends EBase>> belowList = new ArrayList<>(relPattern.getB());

        //duplicate rel
        AsgEBase<Rel> relAsg = new AsgEBase<>(new Rel(counter.incrementAndGet(), pattern.getrType(), pattern.getDir(),
                pattern.getWrapper() != null ? pattern.getWrapper() + "_" + counter.get() : null, 0, pattern.getB()));
        //clone below rel constraints and reset parent with tag (should be wrapper) and eunm
        List<AsgEBase<? extends EBase>> belowCollect = belowList.stream()
                .map(AsgEBase::clone)
                .peek(e -> {
                    e.geteBase().seteNum(counter.incrementAndGet());
                    e.setParent(new ArrayList<>(Arrays.asList(relAsg)));
                    //set new tag for the newly created element
                    if ((e instanceof Tagged) && ((Tagged) e).geteTag() != null) {
                        ((Tagged) e).seteTag(((Tagged) e).geteTag() + "_" + counter.get());
                    }
                }).collect(Collectors.toList());

        relAsg.below(belowCollect);

        // a valid rel patten must have a single next element
        final AsgEBase<? extends EBase> nextAfterRel = relPattern.getNext().get(0);
        final AsgEBase<? extends EBase> clonedPath = clonePath(counter, nextAfterRel, endPattern);

        //add new path to new rel
        relAsg.addNext(clonedPath);
        return clonedPath;
    }


    public static AsgEBase<? extends EBase> clonePath(AtomicInteger counter, AsgEBase<? extends EBase> from, AsgEBase<EBase> to) {
        return AsgQueryUtil.deepCloneWithEnums(counter,
                from,
                e -> {
                    if (e.geteBase() instanceof BaseProp || e.geteBase() instanceof BasePropGroup) {
                        List<AsgEBase<? extends EBase>> list = AsgQueryUtil.pathToNextDescendant(from, to.geteNum());
                        list.remove(to);
                        return list.contains(e.getParents().get(0));
                    }
                    return AsgQueryUtil.pathToNextDescendant(from, to.geteNum()).contains(e);
                }, e -> true);
    }
    //endregion

}
