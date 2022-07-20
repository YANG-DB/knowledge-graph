package org.opensearch.graph.gta.strategy.common;

import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.OntologyTestUtils.DRAGON;
import org.opensearch.graph.model.OntologyTestUtils.PERSON;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.opensearch.graph.model.OntologyTestUtils.OWN;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.rel;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.Rel.Direction.R;
import static org.mockito.Matchers.any;

/**
 * Created by benishue on 12-Mar-17.
 */
public class GoToEntityOpTranslationStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, DRAGON.type,"B")).build();
    }

    @Test
    public void test_entity1_rel2_entity3_goto1() throws Exception {
        AsgQuery query = simpleQuery1("name", "ontName");
        Plan plan = new Plan(
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get())
        );

        TranslationContext context = Mockito.mock(TranslationContext.class);

        GoToEntityOpTranslationStrategy strategy = new GoToEntityOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(0), context);
        GraphTraversal expectedTraversal = __.start().select("A");
        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

}