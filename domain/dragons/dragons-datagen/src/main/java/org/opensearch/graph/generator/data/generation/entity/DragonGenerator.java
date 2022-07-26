package org.opensearch.graph.generator.data.generation.entity;





import org.opensearch.graph.generator.configuration.DragonConfiguration;
import org.opensearch.graph.generator.data.generation.other.PropertiesGenerator;
import org.opensearch.graph.generator.model.entity.Dragon;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class DragonGenerator extends EntityGeneratorBase<DragonConfiguration, Dragon> {

    public DragonGenerator(DragonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Dragon generate() {
        return Dragon.Builder.get()
                .withName(faker.name().firstName() + " " + faker.gameOfThrones().dragon())
                .withBirthDate(faker.date().between(new Date( -46376431374L),new Date( -14819522574L)))
                .withPower(faker.number()
                        .numberBetween(configuration.getMinPower(), configuration.getMaxPower()))
                .withGender(PropertiesGenerator.generateGender())
                .withColor(PropertiesGenerator.generateColor())
                .build();
    }
}
