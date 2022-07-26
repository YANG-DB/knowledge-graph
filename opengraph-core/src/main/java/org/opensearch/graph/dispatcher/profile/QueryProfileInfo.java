package org.opensearch.graph.dispatcher.profile;





import org.opensearch.graph.model.profile.QueryProfileStepInfoData;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;

import java.util.List;
import java.util.stream.Collectors;

public interface QueryProfileInfo {

    Metrics measurements();

    List<QueryProfileStepInfoData> infoData();

    List<QueryProfileStepInfoData> infoData(CreateCursorRequest cursorRequest);

    List<Tuple2<String, Double>> fanOut(CreateCursorRequest cursorRequest);

    class QueryProfileInfoImpl implements QueryProfileInfo {
        private Metrics measurements;

        public QueryProfileInfoImpl(Metrics measurements) {
            this.measurements = measurements;
        }

        @Override
        public Metrics measurements() {
            return measurements;
        }

        public List<QueryProfileStepInfoData> infoData() {
            return this.measurements().getCounts().entrySet().stream()
                    .map(e -> new QueryProfileStepInfoData(e.getKey(), e.getValue(),
                            this.measurements().getAnnotation(e.getKey()).toString())).collect(Collectors.toList());
        }

        @Override
        public List<QueryProfileStepInfoData> infoData(CreateCursorRequest cursorRequest) {
            return this.measurements().getCounts().entrySet().stream()
                    .map(e -> new QueryProfileStepInfoData(e.getKey(), e.getValue(), cursorRequest.getCreatePageRequest().getPageSize(),
                            this.measurements().getAnnotation(e.getKey()).toString())).collect(Collectors.toList());
        }

        @Override
        public List<Tuple2<String, Double>> fanOut(CreateCursorRequest cursorRequest) {
            return this.measurements().getCounts().entrySet().stream()
                    .map(e -> new Tuple2<>(e.getKey(), (double) (e.getValue() / cursorRequest.getCreatePageRequest().getPageSize())))
                    .collect(Collectors.toList());
        }


    }


}
