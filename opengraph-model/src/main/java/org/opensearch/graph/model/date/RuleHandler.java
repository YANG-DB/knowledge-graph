package org.opensearch.graph.model.date;





import com.github.sisyphsu.retree.ReMatcher;

@FunctionalInterface
public interface RuleHandler {

    /**
     * Parse substring[from, to) of the specified string
     *
     * @param chars   The original string in char[]
     * @param matcher The underline ReMatcher
     * @param dt      DateTime to accept parsed properties.
     */
    void handle(CharSequence chars, ReMatcher matcher, DateBuilder dt);

}
