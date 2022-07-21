package org.openserach.graph.asg.strategy.constraint;





import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getEprops;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getRelProps;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.like;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.likeAny;

public class ConstraintExpCharEscapeTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(eProp -> applyExpressionTransformation(context, eProp, EProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(relProp -> applyExpressionTransformation(context, relProp, RelProp.class));
    }
    //endregion

    //region Private Methods
    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class || klass == RelProp.class) {
            BaseProp eProp = (BaseProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            final Constraint con = eProp.getCon();
            if (property.isPresent()
                    && con != null
                    //verify constraint of wildcard type
                    && (con.getOp().equals(like) || con.getOp().equals(likeAny))
                    //verify expression of string type
                    && property.get().getType().equals("string")) {
                con.setExpr(escapeSpecialChars(con.getExpr()));
            }
        }
    }

    private Object escapeSpecialChars(Object expr) {
        if (expr instanceof List) {
            return Stream.ofAll(((List) expr)).map(p -> escape(p)).toJavaList();
        }
        return escape(expr);
    }

    /**
     * Returns a String where those characters that QueryParser
     * expects to be escaped are escaped by a preceding <code>\</code>.
     */
    public static Object escape(Object s) {
        if (s instanceof String) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ((String) s).length(); i++) {
                char c = ((String) s).charAt(i);
                // These characters are part of the query syntax and must be escaped
                if ( c == '?' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            return sb.toString();
        }
        //do nothing
        return s;
    }

    //endregion

}




