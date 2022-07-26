package org.opensearch.graph.unipop.controller.common.appender;





import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import javaslang.collection.Stream;

public class CompositeSearchAppender<TContext> implements SearchAppender<TContext> {
    public enum Mode {
        first,
        all
    }

    //region Constructors
    @SafeVarargs
    public CompositeSearchAppender(Mode mode, SearchAppender<TContext>...searchAppenders) {
        this(mode, Stream.of(searchAppenders));
    }

    public CompositeSearchAppender(Mode mode, Iterable<SearchAppender<TContext>> searchAppenders) {
        this.searchAppenders = Stream.ofAll(searchAppenders).toList();
        this.mode = mode;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        boolean innerAppenderResult = false;

        for(SearchAppender<TContext> searchAppender : this.searchAppenders) {
            innerAppenderResult = searchAppender.append(searchBuilder, context);

            if (innerAppenderResult && this.mode == Mode.first) {
                return true;
            }
        }

        return innerAppenderResult;
    }
    //endregion

    //region Fields
    private Iterable<SearchAppender<TContext>> searchAppenders;
    private Mode mode;
    //endregion
}
