package org.opensearch.graph.executor.cursor.discrete;

import org.opensearch.graph.executor.CompositeTraversalCursorContext;
import org.opensearch.graph.executor.cursor.TraversalCursorContext;
import org.opensearch.graph.model.results.AssignmentsQueryResult;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.model.results.Relationship;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.opensearch.graph.model.results.Assignment.Builder.instance;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

public class InnerQueryCursorTest {

    @Test
    public void testSingleElement() {
        TraversalCursorContext context = Mockito.mock(TraversalCursorContext.class);
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().withEntity(Entity.Builder.instance().withEID("123")
                                .withETag(Stream.of("Child").toJavaSet())
                                .withEType("Entity").build()).build())
                .build();

        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        InnerQueryCursor cursor = new InnerQueryCursor(new CompositeTraversalCursorContext(context,Collections.emptyList()), cursorMock, null);
        AssignmentsQueryResult<Entity, Relationship> nextResults = (AssignmentsQueryResult<Entity, Relationship>) cursor.getNextResults(1000);
        Assert.assertEquals(1, nextResults.getAssignments().size());
        Assert.assertEquals(1, nextResults.getAssignments().get(0).getEntities().size());
        Assert.assertEquals("123", nextResults.getAssignments().get(0).getEntities().get(0).geteID());
        Assert.assertTrue(nextResults.getAssignments().get(0).getEntities().get(0).geteTag().contains("Child"));
        Assert.assertEquals("Entity", nextResults.getAssignments().get(0).getEntities().get(0).geteType());
    }
}
