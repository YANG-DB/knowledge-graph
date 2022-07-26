package org.opensearch.graph.generator.data.generation.entity;





import org.opensearch.graph.generator.configuration.HorseConfiguration;
import org.opensearch.graph.generator.data.generation.other.PropertiesGenerator;
import org.opensearch.graph.generator.model.entity.Horse;
import org.opensearch.graph.generator.util.RandomUtil;

/**
 * Created by benishue on 19/05/2017.
 */
public class HorseGenerator extends EntityGeneratorBase<HorseConfiguration, Horse> {

    public HorseGenerator(HorseConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Horse generate() {
        //Avoiding negative weights
        long weight = Math.max(Math.round(RandomUtil.randomGaussianNumber(configuration.getWeightMean(), configuration.getWeightSD())) ,1);

        return Horse.Builder.get()
                .withName(faker.cat().name().concat(" " + faker.gameOfThrones().character()))
                .withMaxSpeed(faker.number().numberBetween(configuration.getMinSpeed(), configuration.getMaxSpeed()))
                .withColor(PropertiesGenerator.generateColor())
                .withWeight((int) weight)
                .withMaxDistance(Math.toIntExact(faker.number().numberBetween(Math.round(configuration.getMaxDistance() * 0.1), configuration.getMaxDistance())))
                .build();
    }
}
