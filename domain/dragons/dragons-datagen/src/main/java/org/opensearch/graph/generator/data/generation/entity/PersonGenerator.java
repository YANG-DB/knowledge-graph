package org.opensearch.graph.generator.data.generation.entity;





import com.github.javafaker.Name;
import org.opensearch.graph.generator.configuration.PersonConfiguration;
import org.opensearch.graph.generator.data.generation.other.PropertiesGenerator;
import org.opensearch.graph.generator.model.entity.Person;
import org.opensearch.graph.generator.util.DateUtil;
import org.opensearch.graph.generator.util.RandomUtil;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class PersonGenerator extends EntityGeneratorBase<PersonConfiguration, Person> {

    public PersonGenerator(PersonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Person generate() {
        Name fakeName = faker.name();
        Date birthDate = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        long lifeExpectancy = Math.round(RandomUtil.randomGaussianNumber(configuration.getLifeExpectancyMean(), configuration.getLifeExpectancySD()));
        Date deathDate = DateUtil.addYearsToDate(birthDate, (int) lifeExpectancy);
        long height = Math.max(1, Math.round(RandomUtil.randomGaussianNumber(configuration.getHeightMean(), configuration.getHeightSD())));

        return Person.Builder.get()
                .withFirstName(fakeName.firstName())
                .withLastName(fakeName.lastName())
                .withGender(PropertiesGenerator.generateGender())
                .withBirthDate(birthDate)
                .withDeathDate(deathDate)
                .withHeight((int) height)
                .build();
    }
}
