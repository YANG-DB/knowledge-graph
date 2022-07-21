package org.unipop.query.controller;






import org.unipop.structure.UniGraph;

public interface ControllerManagerFactory {
    ControllerManager create(UniGraph graph);
}
