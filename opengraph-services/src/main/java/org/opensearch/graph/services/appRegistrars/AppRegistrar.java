package org.opensearch.graph.services.appRegistrars;




import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;

public interface AppRegistrar {
    void register(Jooby app, AppUrlSupplier appUrlSupplier);
}
