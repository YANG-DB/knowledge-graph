package org.opensearch.graph.asg.strategy;

/*-
 * #%L
 * opengraph-asg
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







import com.google.inject.Inject;
import org.opensearch.graph.asg.strategy.constraint.*;
import org.opensearch.graph.asg.strategy.propertyGrouping.*;
import org.opensearch.graph.asg.strategy.selection.DefaultETagAsgStrategy;
import org.opensearch.graph.asg.strategy.selection.DefaultSelectionAsgStrategy;
import org.opensearch.graph.asg.strategy.type.UntypedRelationInferTypeAsgStrategy;
import org.opensearch.graph.asg.strategy.type.RelationPatternRangeAsgStrategy;
import org.opensearch.graph.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;

import java.util.Arrays;

public class M1AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M1AsgStrategyRegistrar(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DefaultETagAsgStrategy(),
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new RelationPatternRangeAsgStrategy(),
                new UntypedRelationInferTypeAsgStrategy(),
                new Quant1AllQuantGroupingAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new AggFilterTransformationAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
