package org.opensearch.graph.services.suppliers;


import com.google.inject.Inject;
import org.opensearch.graph.model.transport.ExternalMetadata;

import java.util.function.Supplier;

public interface RequestExternalMetadataSupplier extends Supplier<ExternalMetadata> {
    class Impl implements RequestExternalMetadataSupplier {
        //region Constructors
        @Inject
        public Impl(ExternalMetadata externalMetadata) {
            this.externalMetadata = externalMetadata;
        }
        //endregion

        //region ExternalRequestIdSupplier Implementation
        @Override
        public ExternalMetadata get() {
            return this.externalMetadata;
        }
        //endregion

        //region Fields
        private ExternalMetadata externalMetadata;
        //endregion
    }
}
