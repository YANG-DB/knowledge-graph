package org.opensearch.graph.dispatcher.modules;

/*-
 * #%L
 * opengraph-core
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







import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import org.opensearch.graph.dispatcher.descriptors.CursorResourceDescriptor;
import org.opensearch.graph.dispatcher.descriptors.GraphTraversalDescriptor;
import org.opensearch.graph.dispatcher.descriptors.PageResourceDescriptor;
import org.opensearch.graph.dispatcher.descriptors.QueryResourceDescriptor;
import org.opensearch.graph.dispatcher.resource.CursorResource;
import org.opensearch.graph.dispatcher.resource.PageResource;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.descriptors.CompositeDescriptor;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.descriptors.ToStringDescriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.composite.UnionOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.*;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor;
import org.opensearch.graph.model.execution.plan.descriptors.PlanWithCostDescriptor;
import org.opensearch.graph.model.execution.plan.descriptors.QueryDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Query;
import com.typesafe.config.Config;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.jooby.Env;

import java.util.HashMap;
import java.util.Map;

public class DescriptorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        binder.bind(new TypeLiteral<Descriptor<Query>>(){}).to(QueryDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<AsgQuery>>(){}).to(AsgQueryDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<Iterable<PlanOp>>>(){}).toInstance(IterablePlanOpDescriptor.getFull());
        binder.bind(new TypeLiteral<Descriptor<CompositePlanOp>>(){}).to(CompositePlanOpDescriptor.class);

        binder.bind(new TypeLiteral<Descriptor<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .toInstance(new PlanWithCostDescriptor<>(
                        new CompositePlanOpDescriptor(getIterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full)),
                        new ToStringDescriptor<>()));

        /*binder.bind(new TypeLiteral<Descriptor<PlanWithCost<Plan, PlanDetailedCost>>>(){})
                .toInstance(new PlanWithCostDescriptor<>(
                        new CompositePlanOpDescriptor(IterablePlanOpDescriptor.getFull()),
                        new ToStringDescriptor<>()));*/

        binder.bind(new TypeLiteral<Descriptor<GraphTraversal<?, ?>>>(){}).to(GraphTraversalDescriptor.class).asEagerSingleton();

        binder.bind(new TypeLiteral<Descriptor<QueryResource>>(){}).to(QueryResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<CursorResource>>(){}).to(CursorResourceDescriptor.class).asEagerSingleton();
        binder.bind(new TypeLiteral<Descriptor<PageResource>>(){}).to(PageResourceDescriptor.class).asEagerSingleton();
    }
    //endregion

    //Private Methods
    public static IterablePlanOpDescriptor getIterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode mode) {
        IterablePlanOpDescriptor iterablePlanOpDescriptor = new IterablePlanOpDescriptor(mode, null);

        Map<Class<?>, Descriptor<? extends PlanOp>> descriptors = new HashMap<>();
        descriptors.put(CompositePlanOp.class, new CompositePlanOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityJoinOp.class, new EntityJoinOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(UnionOp.class, new UnionOpDescriptor(iterablePlanOpDescriptor));
        descriptors.put(EntityOp.class, new EntityOpDescriptor());
        descriptors.put(RelationOp.class, new RelationOpDescriptor());

        iterablePlanOpDescriptor.setCompositeDescriptor(new CompositeDescriptor<>(descriptors, new ToStringDescriptor<>()));
        return iterablePlanOpDescriptor;
    }
    //endregion
}
