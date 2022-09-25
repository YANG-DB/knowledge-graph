package org.opensearch.graph.asg.strategy.constraint;

import org.opensearch.graph.asg.validation.AsgEntityDuplicateETagValidatorStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.junit.Assert;
import org.junit.Test;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.gt;
import static org.opensearch.graph.model.query.quant.QuantType.all;

public class UnionETagDedupTransformationAsgStrategyTest {
    private Ontology.Accessor ont;

    @Test
    public void testGroupQueryWithSingleTagsByTags(){
        AsgQuery query = AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R1").next(typed(7, "entity3", "C")),
                        optional(11).next(rel(12, "rel4", R,"R2").next(typed(13, "entity4", "E")
                                .next(optional(14).next(rel(15, "rel5", R,"R2").next(typed(16, "entity4", "F")))))))
                .build();
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgEntityDuplicateETagValidatorStrategy validatorStrategy = new AsgEntityDuplicateETagValidatorStrategy();
        Assert.assertFalse(validatorStrategy.apply(query,asgStrategyContext).valid());

        UnionETagDedupTransformationAsgStrategy strategy = new UnionETagDedupTransformationAsgStrategy();
        strategy.apply(query,asgStrategyContext);
        Assert.assertTrue(validatorStrategy.apply(query,asgStrategyContext).valid());

    }

    @Test
    public void testGroupQueryWithDuplicateTagsByTags(){
        AsgQuery query = AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R").next(typed(7, "entity3", "C")),
                        optional(11)
                                .next(rel(12, "rel4", R).next(typed(13, "entity4", "C")
                                        .next(optional(14).next(rel(15, "rel4", R,"R").next(typed(16, "entity4", "B")))))))
                .build();
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgEntityDuplicateETagValidatorStrategy validatorStrategy = new AsgEntityDuplicateETagValidatorStrategy();
        Assert.assertFalse(validatorStrategy.apply(query,asgStrategyContext).valid());

        UnionETagDedupTransformationAsgStrategy strategy = new UnionETagDedupTransformationAsgStrategy();
        strategy.apply(query,asgStrategyContext);
        Assert.assertTrue(validatorStrategy.apply(query,asgStrategyContext).valid());
    }

    @Test
    public void testQueryWithPreservedTags(){
        AsgQuery query = AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "_A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "$B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R").next(typed(7, "entity3", "C")),
                        optional(11)
                                .next(rel(12, "rel4", R).next(typed(13, "entity4", "D")
                                        .next(optional(14).next(rel(15, "rel4", R,"R").next(typed(16, "entity4", "B")))))))
                .build();
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgEntityDuplicateETagValidatorStrategy validatorStrategy = new AsgEntityDuplicateETagValidatorStrategy();
        Assert.assertFalse(validatorStrategy.apply(query,asgStrategyContext).valid());
    }


}
