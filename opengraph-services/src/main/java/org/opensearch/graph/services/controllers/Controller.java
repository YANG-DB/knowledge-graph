package org.opensearch.graph.services.controllers;




public interface Controller<C,D> {
    C driver(D driver);
}
