package org.opensearch.graph.generator.data.generation.entity;





import org.opensearch.graph.generator.configuration.KingdomConfiguration;
import org.opensearch.graph.generator.model.entity.Kingdom;
import org.opensearch.graph.generator.util.DateUtil;
import org.opensearch.graph.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class KingdomGenerator extends EntityGeneratorBase<KingdomConfiguration, Kingdom> {

    private final int MIN_FUND = 10000;
    private final int MAX_FUND = 9999999;
    private final int INDEPENDENCE_DAY_INTERVAL = 15;

    public KingdomGenerator(KingdomConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Kingdom generate() {
        Date startDateOfStory = configuration.getStartDateOfStory();
        return Kingdom.Builder.get()
                .withKing(String.format("King %s %s", faker.name().firstName(), faker.name().lastName()))
                .withQueen(String.format("Queen %s %s", faker.name().firstName(), faker.name().lastName()))
                .withFunds(RandomUtil.uniform(MIN_FUND, MAX_FUND))
                .withIndependenceDay(RandomUtil.randomDate(startDateOfStory, DateUtil.addYearsToDate(startDateOfStory,
                        INDEPENDENCE_DAY_INTERVAL)))
                .build();
    }
}
