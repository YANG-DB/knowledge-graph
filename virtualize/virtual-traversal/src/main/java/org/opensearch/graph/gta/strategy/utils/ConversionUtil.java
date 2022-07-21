package org.opensearch.graph.gta.strategy.utils;


import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.unipop.process.predicate.*;
import org.unipop.process.predicate.ExistsP;
import org.unipop.process.predicate.Text;

import java.util.List;

/**
 * Created by Roman on 10/05/2017.
 */
public class ConversionUtil {
    public static <V> P<V> convertConstraint(Constraint constraint){
        List<Object> range = null;
        //exclusive or - either agg or row base constraint§
        if(constraint.isAggregation()) {
            //support count (group by) by filter predicate
            if (constraint.getCountOp() != null) {
                switch (constraint.getCountOp()) {
                    case eq:
                        return CountFilterP.eq(cast(constraint.getExpr()));
                    case ne:
                        return CountFilterP.neq(cast(constraint.getExpr()));
                    case gt:
                        return CountFilterP.gt(cast(constraint.getExpr()));
                    case le:
                        return CountFilterP.lte(cast(constraint.getExpr()));
                    case lt:
                        return CountFilterP.lt(cast(constraint.getExpr()));
                    case ge:
                        return CountFilterP.gte(cast(constraint.getExpr()));
                    case within:
                        range = CollectionUtil.listFromObjectValue(constraint.getExpr());
                        return CountFilterP.between(cast(range.get(0)), cast(range.get(1)));
                }
            }
            // support (row) filter predicate
        } else {
            if(constraint.getOp()!=null) {
                switch (constraint.getOp()) {
                    case distinct:
                        return DistinctFilterP.distinct();
                    case eq:
                        return P.eq(cast(constraint.getExpr()));
                    case ne:
                        return P.neq(cast(constraint.getExpr()));
                    case gt:
                        return P.gt(cast(constraint.getExpr()));
                    case lt:
                        return P.lt(cast(constraint.getExpr()));
                    case ge:
                        return P.gte(cast(constraint.getExpr()));
                    case le:
                        return P.lte(cast(constraint.getExpr()));
                    case inRange:
                        range = CollectionUtil.listFromObjectValue(constraint.getExpr());
                        return P.between(cast(range.get(0)), cast(range.get(1)));
                    case notInRange:
                        range = CollectionUtil.listFromObjectValue(constraint.getExpr());
                        return P.outside(cast(range.get(0)), cast(range.get(1)));
                    case inSet:
                        return P.within(CollectionUtil.listFromObjectValue(constraint.getExpr()));
                    case notInSet:
                        return P.without(CollectionUtil.listFromObjectValue(constraint.getExpr()));
                    case empty:
                        return P.not(new ExistsP<>());
                    case notEmpty:
                        return new ExistsP<>();
                    case query_string:
                        return Text.queryString((V) constraint.getExpr());
                    case match:
                        return Text.match((V) constraint.getExpr());
                    case match_phrase:
                        return Text.matchPhrase((V) constraint.getExpr());
                    case like:
                        return Text.like((V) constraint.getExpr());
                    case likeAny:
                        return Text.like((V) constraint.getExpr());
                    case startsWith:
                        return Text.prefix((V) constraint.getExpr());
                    case endsWith:
                        return Text.like((V) constraint.getExpr());
                    default:
                        throw new RuntimeException(constraint.getOp() + " not supported constraint");
                }
            }
        }

        return null;
    }

    public static Direction convertDirection(Rel.Direction dir) {
        switch (dir) {
            case R:
                return Direction.OUT;
            case L:
                return Direction.IN;
            default:
                throw new IllegalArgumentException("Not Supported Relation DirectionSchema: " + dir);
        }
    }

    public static String convertDirectionGraphic(Rel.Direction dir) {
        switch (dir) {
            case R: return "-->";
            case L: return "<--";
            case RL: return "<-->";
        }

        return null;
    }

    public static <TIn, TOut> TOut cast(TIn value) {
        return (TOut)value;
    }
}
