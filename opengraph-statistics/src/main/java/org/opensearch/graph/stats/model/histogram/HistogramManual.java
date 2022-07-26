package org.opensearch.graph.stats.model.histogram;





import org.opensearch.graph.stats.model.enums.DataType;
import org.opensearch.graph.stats.model.bucket.BucketRange;
import org.opensearch.graph.stats.model.enums.HistogramType;

import java.util.ArrayList;
import java.util.List;

public class HistogramManual<T> extends Histogram {

    //region Ctrs
    public HistogramManual() {
        super(HistogramType.manual);
    }
    //endregion

    //region Getters & Setters

    public List<BucketRange<T>> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketRange<T>> buckets) {
        this.buckets = buckets;
    }

    //endregion

    //region Fields
    private List<BucketRange<T>> buckets;
    //endregion

    //region Builder
    public static final class Builder {
        private List<BucketRange> buckets;
        private DataType dataType;

        private Builder() {
            this.buckets = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withBuckets(List<BucketRange> buckets) {
            this.buckets = buckets;
            return this;
        }

        public Builder withBucket(BucketRange bucket) {
            this.buckets.add(bucket);
            return this;
        }

        public Builder withDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramManual build() {
            HistogramManual histogramManual = new HistogramManual();
            histogramManual.setBuckets(buckets);
            histogramManual.setDataType(dataType);
            return histogramManual;
        }
    }
    //endregion
}
