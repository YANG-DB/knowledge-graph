package org.opensearch.graph.executor.utils;


import org.opensearch.graph.model.query.Rel;

public class ConversionUtil {
    public static String convertDirectionGraphic(Rel.Direction dir) {
        switch (dir) {
            case R: return "-->";
            case L: return "<--";
            case RL: return "<-->";
        }

        return null;
    }
}
