package org.opensearch.graph.dispatcher.ontology;




import org.opensearch.graph.model.schema.IndexProvider;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by lior.perry on 3/16/2017.
 */
public interface IndexProviderFactory {
    Optional<IndexProvider> get(String id);

    Collection<IndexProvider> getAll();

    IndexProvider add(IndexProvider provider);
}
