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
public interface KnowsEdge {
    static void main(String args[]) throws IOException {

        // Fires
        // Add sideB type
        // redundant field
        // dup + add direction

        Map<String, String> constFields=  new HashMap<>();
        constFields.put(ETLUtils.ENTITY_A_TYPE, ETLUtils.PERSON);
        constFields.put(ETLUtils.ENTITY_B_TYPE, ETLUtils.PERSON);
        AddConstantFieldsTransformer constantFieldsTransformer = new AddConstantFieldsTransformer(constFields, both);
        RedundantFieldTransformer redundantFieldTransformer = new RedundantFieldTransformer(ETLUtils.getClient(),
                ETLUtils.redundant(ETLUtils.KNOWS, Direction.out,"A"),
                ETLUtils.ENTITY_A_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.PERSON,
                ETLUtils.redundant(ETLUtils.KNOWS, Direction.out,"B"),
                ETLUtils.ENTITY_B_ID,
                Stream.ofAll(ETLUtils.indexPartition(ETLUtils.PERSON).getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList(),
                ETLUtils.PERSON);
        DuplicateEdgeTransformer duplicateEdgeTransformer = new DuplicateEdgeTransformer(ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID);

        DateFieldTransformer dateFieldTransformer = new DateFieldTransformer(ETLUtils.START_DATE);
        IdFieldTransformer idFieldTransformer = new IdFieldTransformer(ETLUtils.ID, ETLUtils.DIRECTION_FIELD, ETLUtils.KNOWS);
        ChainedTransformer chainedTransformer = new ChainedTransformer(constantFieldsTransformer,
                duplicateEdgeTransformer,
                redundantFieldTransformer,
                dateFieldTransformer,
                idFieldTransformer
        );

        FileTransformer transformer = new FileTransformer("C:\\demo_data_6June2017\\personsRelations_KNOWS.csv",
                "C:\\demo_data_6June2017\\personsRelations_KNOWS-out.csv",
                chainedTransformer,
                Arrays.asList(ETLUtils.ID, ETLUtils.ENTITY_A_ID, ETLUtils.ENTITY_B_ID, ETLUtils.START_DATE),
                5000);
        transformer.transform();
    }

}
