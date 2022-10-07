package org.opensearch.graph.unipop.schema.providers;

import junit.framework.TestCase;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.opensearch.graph.model.schema.BaseTypeElement.NestedType;
import org.opensearch.graph.model.schema.BaseTypeElement.Type;
import org.opensearch.graph.unipop.step.NestingStepWrapper;

/**
 * test the graph element schema component
 */
public class GraphElementSchemaTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGenerateRegularConstraint() {
        Assert.assertEquals(__.start().has(T.label, "Hi"),
                GraphElementSchema.generateConstraint(Type.of("Hi")).getTraversalConstraint());
    }

    @Test
    public void testGenerateNestedConstraint() {
        Assert.assertEquals(__.start().asAdmin().addStep(new NestingStepWrapper(__.has(T.label, "Hi").asAdmin().getStartStep(), "parent")),
                GraphElementSchema.generateConstraint(NestedType.of("Hi", "parent")).getTraversalConstraint());
    }
}