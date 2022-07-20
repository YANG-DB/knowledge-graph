package org.opensearch.graph.dispatcher.asg;

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

import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.model.asgQuery.*;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Query.QueryUtils;
import org.opensearch.graph.model.query.Start;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.constraint.*;
import org.opensearch.graph.model.resourceInfo.FuseError;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getEprops;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getRelProps;

/**
 * Created by liorp on 12/15/2017.
 */
public class QueryToCompositeAsgTransformer extends QueryToAsgTransformer {
    private OntologyProvider ontologyProvider;

    //region Constructors
    @Inject
    public QueryToCompositeAsgTransformer(OntologyProvider ontologyProvider) {
        super();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgCompositeQuery transform(Query query) {
        AsgCompositeQuery asgQuery = new AsgCompositeQuery(super.transform(query));
        Optional<Ontology> ontology = ontologyProvider.get(query.getOnt());
        apply(asgQuery, new AsgStrategyContext(new Ontology.Accessor(ontology
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology field found for " + query.getOnt()))))));
        return asgQuery;
    }
    //endregion


    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(eProp -> applyExpressionTransformation(query, context, eProp));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(relProp -> applyExpressionTransformation(query, context, relProp));
    }

    //region Private Methods

    private void applyExpressionTransformation(AsgQuery query, AsgStrategyContext context, BaseProp eBase) {
        Optional<Property> property = context.getOntologyAccessor().$property(eBase.getpType());
        if (eBase.getCon() != null) {
            Constraint con = eBase.getCon();
            if (property.isPresent() && con instanceof InnerQueryConstraint) {
                WhereByFacet.JoinType joinType = ((InnerQueryConstraint) con).getJoinType();
                Query innerQuery = ((InnerQueryConstraint) con).getInnerQuery();
                String tagEntity = ((InnerQueryConstraint) con).getTagEntity();
                String projectedFields = ((InnerQueryConstraint) con).getProjectedField();
                Constraint newCon = new JoinParameterizedConstraint(con.getOp(), con.getExpr(),
                        new QueryNamedParameter(innerQuery.getName(), tagEntity + "." + projectedFields),joinType);
                eBase.setCon(newCon);
                //add inner query to chain
                AsgQuery innerAsgQuery = new AsgCompositeQuery(super.transform(innerQuery));
                ((AsgCompositeQuery) query).with(new AsgCompositeQuery(innerAsgQuery));
                apply(innerAsgQuery, context);
            } else if (property.isPresent() && con instanceof WhereByConstraint) {
                //split single query with where constraint into 2 queries
                String tagToSplit = ((WhereByFacet) con).getTagEntity();
                Optional<AsgEBase<EBase>> asgEBase = AsgQueryUtil.getByTag(query.getStart(), tagToSplit);

                //compose inner query
                List<AsgEBase<? extends EBase>> path = AsgQueryUtil.path(query.getStart(), asgEBase.get());
                List<EBase> bases = Stream.of(path).flatMap(Collection::stream).map(AsgEBase::geteBase).collect(Collectors.toList());

                List<AsgEBase<? extends EBase>> pathToProp = AsgQueryUtil.pathToDirectDescendant(asgEBase.get(), EProp.class);
                List<AsgEBase<? extends EBase>> finalPath = AsgQueryUtil.mergePath(path, pathToProp);

                Query innerOriginQuery = QueryUtils.clone(query.getOrigin(), bases);
                innerOriginQuery.setName(query.getName() + AsgCompositeQuery.INNER);


                AsgEBase<Start> clonedStart = AsgQueryUtil.deepClone(query.getStart(), element -> finalPath.contains(element), b -> true);
                AsgQuery innerQuery = AsgQuery.AsgQueryBuilder.anAsgQuery()
                        .withStart(clonedStart)
                        .withName(innerOriginQuery.getName())
                        .withOnt(query.getOnt())
                        .withOrigin(innerOriginQuery)
                        .build();

                //compose parameterized constraint
                WhereByFacet.JoinType joinType = ((WhereByFacet) con).getJoinType();
                String tagEntity = ((WhereByFacet) con).getTagEntity();
                String projectedFields = ((WhereByFacet) con).getProjectedField();
                Constraint newCon = new JoinParameterizedConstraint(con.getOp(), con.getExpr(),
                        new QueryNamedParameter(innerQuery.getName(),
                                tagEntity + "." + projectedFields),joinType);
                eBase.setCon(newCon);

                //add inner query to chain
                AsgQuery innerAsgQuery = new AsgCompositeQuery(innerQuery);
                ((AsgCompositeQuery) query).with(new AsgCompositeQuery(innerAsgQuery));
                apply(innerAsgQuery, context);
            }
        }
    }

}
