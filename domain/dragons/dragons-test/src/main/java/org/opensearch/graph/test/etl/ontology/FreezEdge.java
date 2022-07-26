package org.opensearch.graph.test.etl.ontology;



import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.test.etl.*;
import org.opensearch.graph.test.scenario.ETLUtils;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.opensearch.graph.model.execution.plan.Direction.both;

/**
 * Created by moti on 6/7/2017.
 */
public interface FreezEdge {
    static void main(String args[]) throws IOException {

        // FREEZE
        // Add sideB type
        // redundant field
        // dup + add direction
        Map<String, String> constFields=  new HashMap<>();
        constFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.DRAGON);
        constFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.DRAGON);
        AddConstantFieldsTransformer constantFieldsTransformer = new AddConstantFieldsTransformer(constFields, both);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.FREEZE, Direction.out,"A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.DRAGON,
                ETLUtils.redundant(ETLUtils.FREEZE, Direction.out,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.DRAGON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.DRAGON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(ETLUtils.START_DATE, ETLUtils.END_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ETLUtils.ID, ETLUtils.DIRECTION_FIELD, ETLUtils.FREEZE);
        ChainedTransformer chainedTransformer = new ChainedTransformer(constantFieldsTransformer,
                duplicateEdgeTransformer,
                redundantFieldTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\dragonsRelations_FREEZES.csv",
                "C:\\demo_data_6June2017\\dragonsRelations_FREEZES-out.csv",
                chainedTransformer,
                Arrays.asList(ETLUtils.ID, ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID, ETLUtils.START_DATE, ETLUtils.END_DATE),
                5000);
        transformer.transform();
    }

}
