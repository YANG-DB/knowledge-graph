package org.opensearch.graph.unipop.controller.search;





import org.opensearch.graph.unipop.controller.common.context.CompositeControllerContext;

public interface SearchOrderProviderFactory  {
    SearchOrderProvider build(CompositeControllerContext context);
}
