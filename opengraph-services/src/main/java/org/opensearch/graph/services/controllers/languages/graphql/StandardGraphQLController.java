package org.opensearch.graph.services.controllers.languages.graphql;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.query.graphql.GraphQLToOntologyTransformer;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.services.controllers.SchemaTranslatorController;

import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardGraphQLController implements SchemaTranslatorController {
    public static final String transformerName = "StandardGraphQLController.@transformer";

    //region Constructors
    @Inject
    public StandardGraphQLController(GraphQLToOntologyTransformer transformer, OntologyProvider provider) {
        this.transformer = transformer;
        this.provider = provider;
    }
    //endregion

    //region CatalogController Implementation

    @Override
    public ContentResponse<Ontology> translate(String ontology, String graphQLSchema) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.transform(ontology, graphQLSchema)))
                .compose();
    }

    @Override
    public ContentResponse<String> transform(String ontologyId) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.translate(provider.get(ontologyId)
                        .orElseThrow(() -> new GraphError.GraphErrorException(
                                new GraphError("Ontology Not Found", String.format("Ontology %s is not found in repository", ontologyId)))))))
                .compose();
    }

    //endregion

    //region Private Methods

    //region Fields
    private GraphQLToOntologyTransformer transformer;
    private OntologyProvider provider;

    //endregion

}
