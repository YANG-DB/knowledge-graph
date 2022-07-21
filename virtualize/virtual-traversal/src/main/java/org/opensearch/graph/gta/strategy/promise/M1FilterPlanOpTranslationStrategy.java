package org.opensearch.graph.gta.strategy.promise;


import org.opensearch.graph.gta.strategy.common.CompositePlanOpTranslationStrategy;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.gta.strategy.common.GoToEntityOpTranslationStrategy;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1FilterPlanOpTranslationStrategy extends CompositePlanOpTranslationStrategy {
    //region Constructors
    public M1FilterPlanOpTranslationStrategy() {
        super(
                new EntityOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.filterEntity),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());
    }
    //endregion
}

