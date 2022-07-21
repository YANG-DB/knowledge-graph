package org.opensearch.graph.model.execution.plan.planTree;


/*-
 *
 * BuilderIfc.java - opengraph-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.opensearch.graph.model.execution.plan.IPlan;

import java.util.Optional;

/**
 * Created by lior.perry on 6/26/2017.
 */
public interface BuilderIfc<P extends IPlan> {

    BuilderIfc add(PlanNode child);

    BuilderIfc add(P node, String validationContext);

    int incAndGetPhase();

    BuilderIfc with(P node);

    BuilderIfc selected(Iterable<P> selectedPlans);

    Optional<PlanNode<P>> build();
}
