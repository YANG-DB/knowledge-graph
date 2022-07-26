package org.openserach.graph.asg.strategy;







import com.googlecode.aviator.AviatorEvaluator;
import org.openserach.graph.asg.util.OntologyPropertyTypeFactory;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.*;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.getEprops;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;
import static org.opensearch.graph.model.query.properties.constraint.NamedParameter.$VAL;
import static java.util.Collections.singletonMap;

public class AsgNamedParametersStrategy implements AsgStrategy {


    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        this.query = query;
        this.context = context;
        if (query.getParameters().isEmpty())
            return;

        this.propertyTypeFactory = new OntologyPropertyTypeFactory();

        //first handle more specific OptionalUnaryParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> OptionalUnaryParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> manageOptionalUnaryParameterizedConstraint(new ArrayList<>(query.getParameters()), eProp));

        //handle ParameterizedConstraint
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .filter(prop -> ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                .forEach(eProp -> manageParameterizedConstraint(new ArrayList<>(query.getParameters()), eProp));
    }

    protected void manageParameterizedConstraint(List<NamedParameter> params, EProp eProp) {
        ParameterizedConstraint expr = (ParameterizedConstraint) eProp.getCon();
        String name = expr.getParameter().getName();

        Optional<NamedParameter> parameter = params.stream().filter(p -> p.getName().equals(name)).findAny();
        //in case of singular operator and list of operands - use union of conditions for each query pattern
        if (isArrayOrIterable(parameter.get().getValue()) && isForEachJoin(expr)) {
            AtomicInteger counter = new AtomicInteger(AsgQueryUtil.maxEntityNum(query));
            //repeat for each condition - union on all params
            AsgEBase<Quant1> quant = new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.some));
            AsgEBase<EBase> asgEBase = AsgQueryUtil.nextDescendant(query.getStart(), ETyped.class).get();
            Collection parameterValues = (Collection) parameter.get().getValue();
            //replace each value with the appropriate pattern
            parameterValues.forEach(value -> {
                //clone pattern for each value
                AsgEBase<EBase> pattern = AsgQueryUtil.deepCloneWithEnums(counter, asgEBase, t -> true, t -> true, true);
                //replace named parameter with value...
                Optional<EProp> constraint = AsgQueryUtil.getParameterizedConstraint(pattern, parameter.get());
                manageParameterizedConstraint(Collections.singletonList(new NamedParameter(parameter.get().getName(), value)), constraint.get());
                //add to union
                quant.addNext(pattern);
            });
            query.getStart().setNext(Collections.singletonList(quant));
        //if operand is singular and param values are multiple - create ePropGroup
        } else if (isArrayOrIterable(parameter.get().getValue() ) && ConstraintOp.singleValueOps.contains(eProp.getCon().getOp())) {
            //todo add conditions inside an "AND" EPropGroup
            Optional<AsgEBase<EBase>> ePropAsg = AsgQueryUtil.get(query.getStart(), eProp.geteNum());
            Collection parameterValues = (Collection) parameter.get().getValue();
            //replace eprop with epropGroup
            Collection<EProp> eProps = (Collection<EProp>) parameterValues.stream()
                    .map(value -> EProp.of(eProp.geteNum(), eProp.getpType(),
                            Constraint.of(eProp.getCon().getOp(),
                                    parseValue(eProp, eProp.getCon().getExpr(), new NamedParameter(parameter.get().getName(), value)),
                                    eProp.getCon().getiType())))
                    .collect(Collectors.toList());
            EPropGroup group = new EPropGroup(eProp.geteNum(), eProps);
            ePropAsg.get().seteBase(group);
        } else {
            parameter.ifPresent(namedParameter -> eProp.setCon(
                    Constraint.of(eProp.getCon().getOp(),
                            parseValue(eProp, eProp.getCon().getExpr(), namedParameter),
                            eProp.getCon().getiType())));
        }
    }

    private Object parseValue(EProp eProp, Object exp, NamedParameter namedParameter) {
        Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());

        if (exp == null)
            return namedParameter.getValue();

        if (isArrayOrIterable(exp)) {
            //parse & evaluate expression (function?) according to operator and assign named param value
            Collection eValuatedParams = (Collection) ((Collection) exp).stream()
                    .filter(e -> exp.toString().contains($VAL))
                    .map(e -> AviatorEvaluator.execute(e.toString(),
                            singletonMap($VAL, propertyTypeFactory.supply(property.get(), namedParameter.getValue()))))
                    .collect(Collectors.toList());

            //get all other non evaluated params
            Collection nonEvaluatedParams = (Collection) ((Collection) exp).stream()
                    .filter(e -> !exp.toString().contains($VAL))
                    .collect(Collectors.toList());

            return Stream.concat(
                    eValuatedParams.stream(),
                    nonEvaluatedParams.stream())
                    .collect(Collectors.toList());
        } else if (exp.toString().contains($VAL)) {
            return AviatorEvaluator.execute(exp.toString(),
                    singletonMap($VAL, propertyTypeFactory.supply(property.get(), namedParameter.getValue())));
        }
        return namedParameter.getValue();
    }

    private boolean isForEachJoin(ParameterizedConstraint expr) {
        if (expr instanceof JoinParameterizedConstraint) {
            return ((JoinParameterizedConstraint) expr).getJoinType().equals(WhereByFacet.JoinType.FOR_EACH);
        }
        return isSingleElementOp(expr.getOp());
    }

    protected void manageOptionalUnaryParameterizedConstraint(List<NamedParameter> params, EProp eProp) {
        NamedParameter namedParameter = ((OptionalUnaryParameterizedConstraint) eProp.getCon()).getParameter();
        String name = namedParameter.getName();
        Optional<NamedParameter> parameter = params.stream().filter(p -> p.getName().equals(name)).findAny();
        if (parameter.isPresent()) {
            final Optional<ConstraintOp> optional = ((OptionalUnaryParameterizedConstraint) eProp.getCon()).getOperations().stream()
                    .filter(op -> op.toString().equals(parameter.get().getValue().toString()))
                    .findAny();
            if (optional.isPresent()) {
                eProp.setCon(Constraint.of(optional.get()));
            } else {
                //not present set default value
                eProp.setCon(Constraint.of(eProp.getCon().getOp()));
            }
        } else {
            //not present set default value
            eProp.setCon(Constraint.of(eProp.getCon().getOp()));
        }
    }


    private boolean isArrayOrIterable(Object obj) {
        return isArray(obj) || isIterable(obj);
    }

    private boolean isMultivaluedOp(ConstraintOp op) {
        return multiValueOps.contains(op);
    }

    private boolean isSingleElementOp(ConstraintOp op) {
        return singleValueOps.contains(op);
    }

    private boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    private boolean isIterable(Object obj) {
        return obj != null && Iterable.class.isAssignableFrom(obj.getClass());
    }

    private OntologyPropertyTypeFactory propertyTypeFactory;
    private AsgQuery query;
    private AsgStrategyContext context;

}
