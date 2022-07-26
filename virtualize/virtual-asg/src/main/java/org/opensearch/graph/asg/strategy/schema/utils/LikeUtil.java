package org.opensearch.graph.asg.strategy.schema.utils;





import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.SchematicEProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.exact;
import static org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema.IndexingSchema.Type.ngrams;

public class LikeUtil {
    public static Iterable<EProp> applyWildcardRules(EProp eProp, GraphElementPropertySchema propertySchema) {
        Optional<GraphElementPropertySchema.ExactIndexingSchema> exactIndexingSchema = propertySchema.getIndexingSchema(exact);

        if(!eProp.isConstraint()) return Collections::emptyIterator;

        String expr = (String) eProp.getCon().getExpr();
        if (expr == null || expr.equals("")) {
            return Collections.singletonList(new SchematicEProp(
                    0,
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
        }

        List<String> wildcardParts = Stream.of(expr.split("\\*")).filter(part -> !part.equals("")).toJavaList();

        boolean prefix = !expr.startsWith("*");
        boolean suffix = !expr.endsWith("*");
        boolean exact = prefix && suffix && wildcardParts.size() == 1;

        if (exact) {
            return Collections.singletonList(new SchematicEProp(
                    0,
                    eProp.getpType(),
                    exactIndexingSchema.get().getName(),
                    Constraint.of(ConstraintOp.eq, eProp.getCon().getExpr())));
        }

        List<EProp> newEprops = new ArrayList<>();
        for (int wildcardPartIndex = 0; wildcardPartIndex < wildcardParts.size(); wildcardPartIndex++) {
            String wildcardPart = wildcardParts.get(wildcardPartIndex);

            if (wildcardPartIndex == 0 && prefix) {
                newEprops.add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, wildcardParts.get(0) + "*")));

            } else if (wildcardPartIndex == wildcardParts.size() - 1 && suffix) {
                newEprops.add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardParts.size() - 1))));

            } else if (ngramsApplicable(propertySchema, wildcardPart)) {
                newEprops.add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        propertySchema.getIndexingSchema(ngrams).get().getName(),
                        Constraint.of(ConstraintOp.eq, wildcardPart)));

            } else {
                newEprops.add(new SchematicEProp(
                        0,
                        eProp.getpType(),
                        exactIndexingSchema.get().getName(),
                        Constraint.of(ConstraintOp.like, "*" + wildcardParts.get(wildcardPartIndex) + "*")));
            }
        }

        return newEprops;
    }

    public static Optional<EProp> getWildcardNgramsInsetProp(EProp eProp, GraphElementPropertySchema propertySchema) {
        List<String> words = getWildcardNgrams(propertySchema, eProp.getCon().getExpr().toString());
        if (words.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SchematicEProp(
                0,
                eProp.getpType(),
                propertySchema.getIndexingSchema(ngrams).get().getName(),
                Constraint.of(ConstraintOp.eq, words)));
    }

    public static List<String> getWildcardNgrams(GraphElementPropertySchema propertySchema, String wildcardExpression) {
        Optional<GraphElementPropertySchema.NgramsIndexingSchema> ngramsIndexingSchema = propertySchema.getIndexingSchema(ngrams);
        if (!ngramsIndexingSchema.isPresent()) {
            return Collections.emptyList();
        }

        return Stream.of(wildcardExpression.replace("*", " ").split(" "))
                        .map(String::trim)
                        .filter(word -> word.length() > 0)
                        .flatMap(word -> word.length() > ngramsIndexingSchema.get().getMaxSize() ?
                                Stream.of(word.substring(0, ngramsIndexingSchema.get().getMaxSize()),
                                        word.substring(word.length() - ngramsIndexingSchema.get().getMaxSize(), word.length())) :
                                Stream.of(word))
                        .toJavaList();
    }

    private static boolean ngramsApplicable(GraphElementPropertySchema propertySchema, String wildcardPart) {
        Optional<GraphElementPropertySchema.NgramsIndexingSchema> ngramsIndexingSchema = propertySchema.getIndexingSchema(ngrams);

        if (!wildcardPart.contains(" ") &&
                ngramsIndexingSchema.isPresent() &&
                wildcardPart.length() <= (ngramsIndexingSchema.get()).getMaxSize()) {
            return true;
        }

        return false;
    }
}
