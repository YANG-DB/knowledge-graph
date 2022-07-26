package org.unipop.common.util;








import org.unipop.query.predicates.PredicatesHolder;

@FunctionalInterface
public interface PredicatesTranslator<T> {
    T translate(PredicatesHolder holder) ;

}
