package org.opensearch.graph.generator.data.generation.entity;





import org.opensearch.graph.generator.configuration.GuildConfiguration;
import org.opensearch.graph.generator.model.entity.Guild;
import org.opensearch.graph.generator.util.DateUtil;
import org.opensearch.graph.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class GuildGenerator extends EntityGeneratorBase<GuildConfiguration, Guild> {

    public GuildGenerator(GuildConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Guild generate() {
        Date startDateOfStory = configuration.getStartDateOfStory();
        return Guild.Builder.get()
                .withDescription(faker.lorem().sentence(10, 5))
                .withEstablishDate(RandomUtil.randomDate(startDateOfStory, DateUtil.addYearsToDate(startDateOfStory, 5)))
                .withUrl(faker.internet().url())
                .withIconId(faker.internet().avatar())
                .build();
    }
}
