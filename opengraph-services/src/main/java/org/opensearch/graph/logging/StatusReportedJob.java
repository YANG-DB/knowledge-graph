package org.opensearch.graph.logging;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.resource.store.NodeStatusResource;
import org.jooby.quartz.Scheduled;

public class StatusReportedJob {
        private NodeStatusResource statusResource;

        @Inject
        public StatusReportedJob(NodeStatusResource statusResource) {
            this.statusResource = statusResource;
        }

        @Scheduled("30s; delay=20s; repeat=*")
        public void report() {
            this.statusResource.report();
    }
}
