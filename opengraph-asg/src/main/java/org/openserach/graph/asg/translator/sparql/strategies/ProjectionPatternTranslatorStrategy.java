package org.openserach.graph.asg.translator.sparql.strategies;


import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.TupleExpr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * selection projection query element
 */
public class ProjectionPatternTranslatorStrategy extends UnaryPatternTranslatorStrategy{

    public ProjectionPatternTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies) {
        super(translatorStrategies);
    }

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        if(Projection.class.isAssignableFrom(element.getClass())) {
            //collect projection names - and match them to the tagged elements within the actual query
            ProjectionElemList list = ((Projection) element).getProjectionElemList();
            Map<String, List<AsgEBase<EBase>>> tags = AsgQueryUtil.groupByTags(query.getStart());
            //remove not present tags in the projection
            list.getTargetNames().stream()
                    .filter(e->!tags.containsKey(e)).collect(Collectors.toList())
                    .forEach(rmv->tags.remove(rmv));
            //populate the query
            query.setProjectedFields(tags);
        }
        super.apply(element, query, context);
    }
}
