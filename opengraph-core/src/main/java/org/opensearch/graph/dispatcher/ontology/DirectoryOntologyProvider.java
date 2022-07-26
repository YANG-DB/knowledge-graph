package org.opensearch.graph.dispatcher.ontology;






import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.resourceInfo.GraphError;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DirectoryOntologyProvider implements OntologyProvider {
    private final ObjectMapper mapper = new ObjectMapper();
    private String dirName;

    //region Constructors
    public DirectoryOntologyProvider(String dirName) {
        this.dirName = dirName;
        this.ontologies = new HashMap<>();
        String currentDir = System.getProperty("user.dir");

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).getFile());
        }
        if (dir.exists()) {
            this.ontologies =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                            .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                            .filter(file -> !FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("transformation"))
                            .toJavaMap(file -> {
                                try {
                                    Ontology ontology = OntologyFinalizer.finalize(mapper.readValue(file, Ontology.class));
                                    return new Tuple2<>(ontology.getOnt(), ontology);
                                } catch (IOException e) {
                                    return new Tuple2<>(FilenameUtils.getBaseName(file.getName()), new Ontology());
                                }
                            });
        }
    }
    //endregion

    //region OntologyProvider Implementation
    @Override
    public Optional<Ontology> get(String id) {
        return Optional.ofNullable(this.ontologies.get(id));
    }

    @Override
    public Collection<Ontology> getAll() {
        return ontologies.values();
    }

    @Override
    public synchronized Ontology add(Ontology ontology) {
        ontologies.put(ontology.getOnt(), OntologyFinalizer.finalize(ontology));
        //store locally
        String currentDir = System.getProperty("user.dir");
        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).getFile());
        }

        if (dir.exists()) {
            Path path = Paths.get(dir.getAbsolutePath()+"/"+ontology.getOnt()+".json");
            try {
                Files.write(path, mapper.writeValueAsBytes(ontology));
            } catch (IOException e) {
                throw new GraphError.GraphErrorException("Failed writing file for new Ontology ["+ontology.getOnt()+"] ",e.getCause());
            }
        }

        return ontology;
    }
    //endregion

    //region Fields
    private Map<String, Ontology> ontologies;
    //endregion
}
