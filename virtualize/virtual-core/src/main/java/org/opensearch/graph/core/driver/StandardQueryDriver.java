package org.opensearch.graph.core.driver;

/*-
 * #%L
 * virtual-core
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
import org.opensearch.graph.dispatcher.driver.CursorDriver;
import org.opensearch.graph.dispatcher.driver.PageDriver;
import org.opensearch.graph.dispatcher.driver.QueryDriverBase;
import org.opensearch.graph.dispatcher.epb.PlanSearcher;
import org.opensearch.graph.dispatcher.query.JsonQueryTransformerFactory;
import org.opensearch.graph.dispatcher.query.QueryTransformer;
import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.dispatcher.resource.store.ResourceStore;
import org.opensearch.graph.dispatcher.urlSupplier.AppUrlSupplier;
import org.opensearch.graph.dispatcher.validation.QueryValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.descriptors.AsgQueryDescriptor;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.QueryMetadata;
import org.opensearch.graph.model.transport.CreateQueryRequest;

import static org.opensearch.graph.model.transport.CreateQueryRequestMetadata.QueryType.concrete;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardQueryDriver extends QueryDriverBase {
    //region Constructors
    @Inject
    public StandardQueryDriver(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            JsonQueryTransformerFactory transformerFactory,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        super(cursorDriver, pageDriver, queryTransformer, transformerFactory , queryValidator, resourceStore, urlSupplier);
        this.queryRewriter = queryRewriter;
        this.planSearcher = planSearcher;
    }
    //endregion

    //region QueryDriverBase Implementation
    @Override
    protected QueryResource createResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata metadata) {

        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planWithCost(metadata, asgQuery);

        return new QueryResource(request, query, asgQuery, metadata, planWithCost, null);
    }

    protected PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query) {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = PlanWithCost.EMPTY_PLAN;

        //calculate execution plan - only when explicitly asked and type is not parameterized - cant count of evaluate "named" parameters
        if (metadata.isSearchPlan() && metadata.getType().equals(concrete)) {
            planWithCost = this.planSearcher.search(query);

            if (planWithCost == null) {
                throw new IllegalStateException("No valid plan was found for query " + (AsgQueryDescriptor.toString(query)));
            }
        }
        return planWithCost;
    }

    protected AsgQuery rewrite(AsgQuery asgQuery) {
        return this.queryRewriter.transform(asgQuery);
    }
    //endregion

    //region Fields
    private QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;



    //endregion
}
