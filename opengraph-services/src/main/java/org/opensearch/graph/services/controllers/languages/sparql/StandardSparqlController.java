package org.opensearch.graph.services.controllers.languages.sparql;

/*-
 * #%L
 * opengraph-services
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.query.rdf.OWLToOntologyTransformer;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.model.transport.ContentResponse.Builder;
import org.opensearch.graph.services.controllers.SchemaTranslatorController;

import java.util.Arrays;
import java.util.Optional;

import static org.opensearch.graph.model.transport.Status.*;

/**
 * Created by lior.perry on 19/02/2017.
 */
public class StandardSparqlController implements SchemaTranslatorController {
    public static final String transformerName = "StandardSparqlController.@transformer";

    //region Constructors
    @Inject
    public StandardSparqlController(OWLToOntologyTransformer transformer, OntologyProvider provider) {
        this.transformer = transformer;
        this.provider = provider;
    }
    //endregion


    @Override
    public ContentResponse<Ontology> translate(String ontology, String owlSchema) {
        return Builder.<Ontology>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.transform(ontology, Arrays.asList(owlSchema))))
                .compose();
    }

    @Override
    public ContentResponse<String> transform(String ontologyId) {
        return Builder.<String>builder(OK, NOT_FOUND)
                .data(Optional.of(this.transformer.translate(provider.get(ontologyId)
                        .orElseThrow(() -> new GraphError.GraphErrorException(
                                new GraphError("Ontology Not Found", String.format("Ontology %s is not found in repository", ontologyId)))))
                        .stream().reduce("", String::concat)
                ))
                .compose();
    }

    //endregion

    //region Private Methods

    //region Fields
    private OWLToOntologyTransformer transformer;
    private OntologyProvider provider;

    //endregion

}
