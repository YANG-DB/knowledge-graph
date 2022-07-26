package org.unipop.process.predicate;







import org.apache.tinkerpop.gremlin.process.traversal.P;

public class DistinctFilterP<V> extends P<V> {

    private DistinctFilterP() {
        super(null, null);
    }

    public static <V> DistinctFilterP<V> distinct() {
        return new DistinctFilterP<V>();
    }

    @Override
    public String toString() {
        return "DistinctFilterP{}";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof P &&
                ((P) other).getClass().equals(this.getClass());

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static boolean hasDistinct(org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep<?> step) {
        return step.getHasContainers().stream().anyMatch(c->DistinctFilterP.class.isAssignableFrom(c.getPredicate().getClass()));
    }

//endregion
}
