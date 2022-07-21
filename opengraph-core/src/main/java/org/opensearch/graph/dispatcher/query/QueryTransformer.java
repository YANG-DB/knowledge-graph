package org.opensearch.graph.dispatcher.query;




public interface QueryTransformer<QIn, QOut> {
    QOut transform(QIn query);
}
