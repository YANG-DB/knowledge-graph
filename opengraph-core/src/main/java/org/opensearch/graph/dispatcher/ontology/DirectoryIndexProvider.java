package org.opensearch.graph.dispatcher.ontology;




import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.schema.IndexProvider;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DirectoryIndexProvider implements IndexProviderFactory {
    //region Constructors
    public DirectoryIndexProvider(String dirName) throws URISyntaxException {
        this.map = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if(!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).toURI());
        }
        if (dir.exists()) {
            this.map =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("conf"))
                    .map(file -> {
                        try {
                            return mapper.readValue(file, IndexProvider.class);
                        } catch (IOException e) {
                            throw new FuseError.FuseErrorException( "Error reading index provider ",e,new FuseError("Error reading index provider ",file.getName()));
                        }
                    }).toJavaMap(provider -> new Tuple2<>(provider.getOntology(), provider));
        }
    }
    //endregion

    //region OntologyProvider Implementation
    @Override
    public Optional<IndexProvider> get(String id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<IndexProvider> getAll() {
        return map.values();
    }

    @Override
    public IndexProvider add(IndexProvider indexProvider) {
        map.put(indexProvider.getOntology(),indexProvider);
        return indexProvider;
    }
    //endregion

    //region Fields
    private Map<String, IndexProvider> map;
    //endregion
}
