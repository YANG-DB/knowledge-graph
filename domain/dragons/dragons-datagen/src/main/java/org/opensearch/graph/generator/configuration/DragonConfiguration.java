package org.opensearch.graph.generator.configuration;






import org.apache.commons.configuration.Configuration;

import java.io.File;
import java.util.Date;

/**
 * Created by benishue on 18-May-17.
 */
public class DragonConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public DragonConfiguration(final Configuration configuration) {
        super(
                configuration.getInt("dragon.numberOfNodes"),
                configuration.getInt("dragon.edgesPerNode"),
                System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath") + File.separator
                        + configuration.getString("dragon.dragonsResultsCsvFileName"),
                System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath") + File.separator
                        + configuration.getString("dragon.dragonsRelationsCsvFileName")
        );

        //01/01/1900 00:00:00 GMT epoch time in milliseconds
        this.startDateOfStory = new Date(configuration.getLong("dragon.startDateOfStory"));
        //01/01/2000 00:00:00 GMT epoch time in milliseconds
        this.endDateOfStory = new Date(configuration.getLong("dragon.endDateOfStory"));
        this.fireProbability = configuration.getDouble("dragon.fireProbability");
        this.freezProbability = configuration.getDouble("dragon.freezProbability");
        this.minUniqueInteractions = configuration.getInt("dragon.minUniqueInteractions");
        this.maxUniqueInteractions = configuration.getInt("dragon.maxUniqueInteractions");
        this.freezMaxDuraution = configuration.getInt("dragon.freezMaxDuraution");
        this.maxPower = configuration.getInt("dragon.maxPower");
        this.minPower = configuration.getInt("dragon.minPower");
        this.idPrefix = configuration.getString("dragon.idPrefix");
        this.fireMaxTemperature = configuration.getInt("dragon.fireMaxTemperature");
        this.fireMinTemperature = configuration.getInt("dragon.fireMinTemperature");

    }
    //endregion

    //region Getters
    public Date getStartDateOfStory() {
        return startDateOfStory;
    }

    public Date getEndDateOfStory() {
        return endDateOfStory;
    }

    public int getMinPower() {
        return minPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMaxUniqueInteractions() {
        return maxUniqueInteractions;
    }

    public int getMinUniqueInteractions() {
        return minUniqueInteractions;
    }

    public double getFreezProbability() {
        return freezProbability;
    }

    public double getFireProbability() {
        return fireProbability;
    }

    public int getFreezMaxDuraution() {
        return freezMaxDuraution;
    }

    public int getFireMaxTemperature() {
        return fireMaxTemperature;
    }

    public int getFireMinTemperature() {
        return fireMinTemperature;
    }

    public String getIdPrefix() {
        return idPrefix;
    }
    //endregion

    //region Fields
    private final Date startDateOfStory;
    private final Date endDateOfStory;
    private final int minPower;
    private final int maxPower;
    private final int maxUniqueInteractions;
    private final int minUniqueInteractions;
    private final double freezProbability;
    private final double fireProbability;
    private final int freezMaxDuraution;
    private final int fireMaxTemperature;
    private final int fireMinTemperature;
    private final String idPrefix;
    //endregion
}
