package org.opensearch.graph.generator.data.generation.other;

/*-
 * #%L
 * dragons-datagen
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
