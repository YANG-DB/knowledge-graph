package org.opensearch.graph.model.execution.plan;




import org.opensearch.graph.model.query.Rel;

/**
 * Created by lior.perry on 22/02/2017.
 */
public enum Direction {
    in,
    out,
    both;

    public Direction reverse() {
        if (this == both)
            return both;
        return in == this ? out : in;
    }

    public static Rel.Direction reverse(Rel.Direction dir) {
        return of(dir).reverse().to();
    }

    public static Direction of(Rel.Direction dir) {
        switch (dir) {
            case R:
                return out;
            case L:
                return in;
            case RL:
                return both;
        }
        return both;
    }

    public Rel.Direction to() {
        switch (this) {
            case both:
                return Rel.Direction.RL;
            case in:
                return Rel.Direction.L;
            case out:
                return Rel.Direction.R;

        }
        return Rel.Direction.RL;
    }
}
