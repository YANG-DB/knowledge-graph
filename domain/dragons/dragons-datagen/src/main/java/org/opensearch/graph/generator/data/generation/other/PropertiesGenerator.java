package org.opensearch.graph.generator.data.generation.other;





import org.opensearch.graph.generator.model.enums.Color;
import org.opensearch.graph.generator.model.enums.Gender;
import org.opensearch.graph.generator.util.RandomUtil;

import java.util.Random;

/**
 * Created by benishue on 15-May-17.
 */
public class PropertiesGenerator {

    private static final Random rand = new Random();

    private PropertiesGenerator() {
        throw new IllegalAccessError("Utility class");
    }

    // 50% Chance for each gender
    public static Gender generateGender() {
        return (rand.nextBoolean() ? Gender.MALE : Gender.FEMALE);
    }

    public static Color generateColor() {
        return (RandomUtil.randomEnum(Color.class));
    }


}
