package org.opensearch.graph.executor.modules.discrete;





import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory.Binding;
import org.opensearch.graph.dispatcher.modules.ModuleBase;
import org.opensearch.graph.executor.cursor.discrete.*;
import org.opensearch.graph.model.transport.cursor.*;
import com.typesafe.config.Config;
import org.jooby.Env;

public class CursorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new PathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateForwardOnlyPathTraversalCursorRequest.CursorType,
                CreateForwardOnlyPathTraversalCursorRequest.class,
                new ForwardOnlyPathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CountCursorRequest.CursorType,
                CountCursorRequest.class,
                new CountTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                FindPathTraversalCursorRequest.CursorType,
                FindPathTraversalCursorRequest.class,
                new FindPathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphCursorRequest.CursorType,
                CreateGraphCursorRequest.class,
                new GraphTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphQLCursorRequest.CursorType,
                CreateGraphQLCursorRequest.class,
                new GraphQLTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphHierarchyCursorRequest.CursorType,
                CreateGraphHierarchyCursorRequest.class,
                new NewGraphHierarchyTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateHierarchyFlattenCursorRequest.CursorType,
                CreateHierarchyFlattenCursorRequest.class,
                new HierarchyFlattenCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateInnerQueryCursorRequest.CursorType,
                CreateInnerQueryCursorRequest.class,
                new InnerQueryCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateCsvCursorRequest.CursorType,
                CreateCsvCursorRequest.class,
                new CsvTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                ProjectionCursorRequest.CursorType,
                ProjectionCursorRequest.class,
                new IndexProjectionCursor.Factory()));
    }
    //endregion
}
