package org.unipop.process;








import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface Profiler {
    String PROFILER = "profiler";

    MutableMetrics get();

    class Noop implements Profiler {
        public static Noop instance = new Noop();
        public static final String NOOP_PROFILER = "Noop profiler";
        public static MutableMetrics measurement = new MutableMetrics() {
            @Override
            public void setAnnotation(String key, Object value) {}

            @Override
            public void incrementCount(String key, long incr) {}

            @Override
            public long getDuration(TimeUnit units) {
                return -1;
            }

            @Override
            public void setCount(String key, long val) {}

            @Override
            public Long getCount(String countKey) {
                return Long.valueOf(-1);
            }

            @Override
            public Map<String, Long> getCounts() {
                return Collections.singletonMap(NOOP_PROFILER,-1L);
            }

            @Override
            public String getName() {
                return NOOP_PROFILER;
            }

            @Override
            public String getId() {
                return NOOP_PROFILER;
            }

            @Override
            public MutableMetrics getNested(String metricsId) {
                return this;
            }

            @Override
            public Map<String, Object> getAnnotations() {
                return Collections.singletonMap(NOOP_PROFILER,NOOP_PROFILER);
            }

            @Override
            public Object getAnnotation(String key) {
                return NOOP_PROFILER;
            }
        };

        @Override
        public MutableMetrics get() {
            return measurement;
        }
    }

    class Impl implements Profiler {
        //region Constructors
        public Impl() {
            this.measurements = new MutableMetrics() {};
        }
        //endregion

        //region Public Methods
        public Profiler add(String name) {
            this.measurements = new MutableMetrics(name,name);
            return this;
        }

        //endregion
        @Override
        public MutableMetrics get() {
            return measurements;
        }

        @Override
        public String toString() {
            return StringUtils.join(measurements.getCounts());
        }

        //region Fields
        private MutableMetrics measurements;
        //endregion

    }
}
