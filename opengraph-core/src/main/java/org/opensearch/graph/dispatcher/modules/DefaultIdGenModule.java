package org.opensearch.graph.dispatcher.modules;





import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.model.Range;
import com.typesafe.config.Config;
import org.jooby.Env;

import java.util.List;

public class DefaultIdGenModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config config, Binder binder) {
        binder.bind(new TypeLiteral<IdGeneratorDriver<Range>>(){}).toInstance(new IdGeneratorDriver<Range>() {
            @Override
            public Range getNext(String genName, int numIds) {
                return new Range(1l,2l);
            }

            @Override
            public boolean init(List<String> names) {
                return true;
            }
        });
    }
}
