package org.opensearch.graph.executor;



import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import org.opensearch.graph.dispatcher.cursor.CompositeCursorFactory;

public class CursorBindingModule extends PrivateModule {
    //region Constructors
    public CursorBindingModule(CompositeCursorFactory.Binding binding) {
        this.binding = binding;
    }
    //endregion

    //region PrivateModule Implementation
    @Override
    protected void configure() {
        Multibinder.newSetBinder(this.binder(), CompositeCursorFactory.Binding.class).addBinding().toInstance(this.binding);
        this.expose(CompositeCursorFactory.Binding.class);
    }
    //endregion

    //region Fields
    private CompositeCursorFactory.Binding binding;
    //endregion
}
