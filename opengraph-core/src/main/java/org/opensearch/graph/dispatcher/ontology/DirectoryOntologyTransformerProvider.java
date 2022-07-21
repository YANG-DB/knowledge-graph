package org.opensearch.graph.dispatcher.ontology;




import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.ontology.transformer.OntologyTransformer;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryOntologyTransformerProvider implements OntologyTransformerProvider {
    //region Constructors
    public DirectoryOntologyTransformerProvider(String dirName) throws URISyntaxException {
        this.transformations = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).toURI());
        }
        if (dir.exists()) {
            this.transformations =
                    Stream.of(Objects.requireNonNull(dir.listFiles() == null ? new File[0] : dir.listFiles()))
                            .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                            .filter(file -> FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("transformation"))
                            .toJavaMap(file -> {
                                try {
                                    OntologyTransformer ontologyTransformer = mapper.readValue(file, OntologyTransformer.class);
                                    return new Tuple2<>(ontologyTransformer.getOnt(),ontologyTransformer);
                                } catch (IOException e) {
                                    return new Tuple2<>(FilenameUtils.getBaseName(file.getName()), new OntologyTransformer());
                                }
                            });
        }
    }
    //endregion

    //region Fields
    private Map<String, OntologyTransformer> transformations;

    @Override
    public Optional<OntologyTransformer> transformer(String id) {
        return Optional.ofNullable(this.transformations.get(id));

    }

    @Override
    public Collection<OntologyTransformer> transformation() {
        return Collections.unmodifiableCollection(transformations.values());
    }
//endregion
}
