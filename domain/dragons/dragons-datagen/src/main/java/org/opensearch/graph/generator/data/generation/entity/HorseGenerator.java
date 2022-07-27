package org.opensearch.graph.generator.data.generation.entity;

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
