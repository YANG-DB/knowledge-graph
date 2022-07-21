package org.openserach.graph.asg.strategy.constraint;




import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.*;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getEprops;
import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getRelProps;

public class ConstraintExpLowercaseTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public ConstraintExpLowercaseTransformationAsgStrategy(Collection<String> fields) {
        this.fields = new HashSet<>(fields);
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> fields.contains(prop.getpType()))
                .forEach(eProp -> applyExpressionTransformation(context, eProp, BaseProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> fields.contains(prop.getpType()))
                .forEach(relProp -> applyExpressionTransformation(context, relProp, BaseProp.class));
    }
    //endregion

    //region Private Methods
    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (BaseProp.class.isAssignableFrom(klass)) {
            BaseProp eProp = (BaseProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            final Constraint con = eProp.getCon();
            if (con != null && property.isPresent() && property.get().getType().equals("string")) {
                if (con.getExpr() instanceof List) {
                    con.setExpr(Stream.ofAll((List) con.getExpr()).map(e->e.toString().toLowerCase()).toJavaList());
                } else if(con.getExpr() instanceof String){
                    con.setExpr(con.getExpr().toString().toLowerCase());
                }
            }
        }
    }
    //endregion

    //region Fields
    private final Set<String> fields;
    //endregion
}




