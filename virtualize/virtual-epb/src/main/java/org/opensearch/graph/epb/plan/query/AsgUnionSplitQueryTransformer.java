package org.opensearch.graph.epb.plan.query;





import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgQueryVisitor;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;

import java.util.*;
import java.util.function.Predicate;

public class AsgUnionSplitQueryTransformer implements QueryTransformer<AsgQuery, Iterable<AsgQuery>> {
    private QueryTransformer<AsgQuery, AsgQuery> queryTransformer;
    //region QueryTransformer Implementation

    public AsgUnionSplitQueryTransformer(QueryTransformer<AsgQuery, AsgQuery> queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    @Override
    public Iterable<AsgQuery> transform(AsgQuery query) {
        if(query==null)
            throw new IllegalArgumentException("Query was null - probably serialization from input failed");

        return Stream.ofAll(new PermutationVisitor(Collections.emptyMap()).visit(query.getStart()))
                .map(permutation -> AsgQueryUtil.transform(
                        query.getStart(),
                        asgEBase -> AsgEBase.Builder.get().withEBase(skipPermutationStops(asgEBase, permutation).geteBase()).build(),
                        asgEBase -> true,
                        AsgEBase::getB,
                        asgEBase -> skipPermutationStops(asgEBase, permutation).getNext()))
                .map(permutationQueryStart -> AsgQuery.AsgQueryBuilder.anAsgQuery()
                        .withName(query.getName())
                        .withOrigin(query.getOrigin())
                        .withOnt(query.getOnt())
                        .withStart((AsgEBase<Start>) (AsgEBase<?>) permutationQueryStart)
                        .withElements(new ArrayList<>(AsgQueryUtil.elements(permutationQueryStart)))
                        .withParams(query.getParameters())
                        .build())
                .map(q -> queryTransformer.transform(q))
                .toJavaList();
    }
    //endregion

    //region private methods
    private static AsgEBase<? extends EBase> skipPermutationStops(AsgEBase<? extends EBase> asgEBase, Map<Integer, Integer> permutation) {
        AsgEBase<? extends EBase> currentElement = asgEBase;
        while (permutation.containsKey(currentElement.geteNum())) {
            for (AsgEBase<? extends EBase> childEBase : currentElement.getNext()) {
                if (childEBase.geteNum() == permutation.get(currentElement.geteNum())) {
                    currentElement = childEBase;
                    break;
                }
            }
        }
        return currentElement;
    }
    //endregion

    //region PermutationVisitor
    public static class PermutationVisitor extends AsgQueryVisitor<Set<Map<Integer, Integer>>> {
        //region Constructors
        public PermutationVisitor(Map<Integer, Integer> currentPermutation) {
            super(
                    asgEBase -> leafPredicate.test(asgEBase),
                    asgEBase -> Collections.singleton(currentPermutation),
                    asgEBase -> true,
                    asgEBase -> Collections.emptyList(),
                    AsgEBase::getNext,
                    asgEBase -> Collections.emptySet(),
                    asgEBase -> someQuantPredicate.test(asgEBase.getParents().get(0)) ?
                            new PermutationVisitor(new MapBuilder<>(currentPermutation)
                                    .put(asgEBase.getParents().get(0).geteNum(), asgEBase.geteNum()).get())
                                    .visit(asgEBase) :
                            new PermutationVisitor(currentPermutation).visit(asgEBase),
                    PermutationVisitor::consolidatePermutations,
                    PermutationVisitor::consolidatePermutations);
        }
        //endregion

        //region Private Methods
        private static Set<Map<Integer, Integer>> consolidatePermutations(Set<Map<Integer, Integer>> permutations1, Set<Map<Integer, Integer>> permutations2) {
            if (permutations1 == null && permutations2 != null) {
                return permutations2;
            }

            if (permutations2 == null && permutations1 != null) {
                return permutations1;
            }

            if (permutations1 == null && permutations2 == null) {
                return Collections.emptySet();
            }

            Set<Map<Integer, Integer>> consolidatedPermutations = new HashSet<>();

            for (Map<Integer, Integer> permutation1 : permutations1) {
                for (Map<Integer, Integer> permutation2 : permutations2) {

                    boolean areDisjoint = true;
                    for (Integer permutation2Key : permutation2.keySet()) {
                        if (permutation1.keySet().contains(permutation2Key)) {
                            areDisjoint = false;
                            break;
                        }
                    }

                    if (areDisjoint) {
                        consolidatedPermutations.add(new MapBuilder<>(permutation1).putAll(permutation2).get());
                    } else {
                        consolidatedPermutations.add(permutation1);
                        consolidatedPermutations.add(permutation2);
                    }
                }
            }

            return consolidatedPermutations;
        }
        //endregion
    }
    //endregion

    //region Fields
    private static Predicate<AsgEBase<? extends EBase>> someQuantPredicate = asgEBase ->
            asgEBase.geteBase().getClass().equals(Quant1.class) &&
                    ((Quant1) asgEBase.geteBase()).getqType().equals(QuantType.some);

    private static Predicate<AsgEBase<? extends EBase>> leafPredicate = asgEBase -> asgEBase.getNext().isEmpty();
    //endregion
}
