package org.opensearch.graph.services.suppliers;




import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.driver.IdGeneratorDriver;
import org.opensearch.graph.model.Range;
import com.twitter.snowflake.sequence.IdSequence;
import com.twitter.snowflake.support.IdSequenceFactory;

/**
 * Created by Roman on 4/9/2018.
 */
public class SnowflakeRequestIdSupplier implements RequestIdSupplier {
    private IdGeneratorDriver<Range> idGeneratorDriver;

    //region Constructors
    @Inject
    public SnowflakeRequestIdSupplier(IdGeneratorDriver<Range> idGeneratorDriver) {
        this.idGeneratorDriver = idGeneratorDriver;
    }

    private void init() {
        IdSequenceFactory idSequenceFactory = new IdSequenceFactory();
        idSequenceFactory.setTimeBits(41);
        idSequenceFactory.setWorkerBits(6);
        idSequenceFactory.setSeqBits(16);

        Range workerIdRange = idGeneratorDriver.getNext("workerId", 1);
        if(workerIdRange != null){
            workerId = workerIdRange.getLower() % (2 ^ 6);
            idSequenceFactory.setWorkerId(workerId);
        }else {
            idSequenceFactory.setWorkerId(1L);
        }
        this.sequence = idSequenceFactory.create();
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        if(sequence==null) {
            init();
        }
        return String.format("FR%s", this.sequence.nextId());
    }

    public long getWorkerId() {
        return workerId;
    }

    //endregion

    //region Fields
    private long workerId;
    private IdSequence sequence;
    //endregion
}
