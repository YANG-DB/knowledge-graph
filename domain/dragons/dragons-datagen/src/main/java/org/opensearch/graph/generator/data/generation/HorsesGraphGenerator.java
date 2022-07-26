package org.opensearch.graph.generator.data.generation;





import org.opensearch.graph.generator.configuration.HorseConfiguration;
import org.opensearch.graph.generator.data.generation.entity.HorseGenerator;
import org.opensearch.graph.generator.model.entity.EntityBase;
import org.opensearch.graph.generator.model.entity.Horse;
import org.opensearch.graph.generator.util.CsvUtil;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.opensearch.graph.generator.data.generation.graph.GraphGeneratorBase.BUFFER;

/**
 * Created by benishue on 05/06/2017.
 */
public class HorsesGraphGenerator {

    public static final String[] HORSE_HEADER = {"id", "name", "color", "weight", "maxSpeed", "distance"};
    private final Logger logger = LoggerFactory.getLogger(HorsesGraphGenerator.class);

    public HorsesGraphGenerator(final HorseConfiguration configuration) {
        this.horseConf =configuration;
    }

    public List<String> generateHorsesGraph() {
        List<Horse> horses = generateHorses();
        return Stream.ofAll(horses).map(EntityBase::getId).toJavaList();
    }

    public List<Horse> generateHorses() {
        List<Horse> horses = new ArrayList<>();
        List<String[]> horsesRecords = new ArrayList<>();
        horsesRecords.add(0, HORSE_HEADER);
        try {
            HorseGenerator generator = new HorseGenerator(horseConf);
            int elements = horseConf.getNumberOfNodes();

            for (int i = 0; i < elements; i++) {
                Horse horse = generator.generate();
                horse.setId(Integer.toString(i));
                horses.add(horse);
                horsesRecords.add(horse.getRecord());
                if(elements % BUFFER == 0)
                    logger.info("writing to file ... "+ BUFFER +" elements");
            }
            //Write graph
            CsvUtil.appendResults(horsesRecords, horseConf.getEntitiesFilePath());

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return horses;
    }

    //region Fields
    private final HorseConfiguration horseConf;

    /**
     * cleanup intermediate files
     */
    public void Cleanup() {}
    //endregion

}
