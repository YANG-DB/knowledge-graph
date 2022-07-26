package org.opensearch.graph.test.scenario;



import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.graph.model.GlobalConstants;

import static org.opensearch.graph.test.scenario.ETLUtils.splitFileToChunks;

/**
 * Created by Roman on 07/06/2017.
 */
public class TimePartitionFile {
    public static void mainOwnDragons(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_OWNS_DRAGON-out.csv", "C:\\demo_data_6June2017\\own_dragons_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainOwnHorses(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_OWNS_HORSE-out.csv", "C:\\demo_data_6June2017\\own_horses_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainFreeze(String[] args) {
        splitFileToChunks("E:\\fuse_data\\edges\\dragonsRelations_FREEZES-out.csv", "E:\\fuse_data\\edges\\dragonsRelations_FREEZES_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainMemberOf(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\guildsRelations_MEMBER_OF_GUILD-out.csv", "C:\\demo_data_6June2017\\member_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainKnows(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\personsRelations_KNOWS-out.csv", "C:\\demo_data_6June2017\\knows_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainOriginatedDragon(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_DRAGON-out.csv", "C:\\demo_data_6June2017\\dragon_originated_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");

    }

    public static void mainOriginatedHorse(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_ORIGINATED_HORSE-out.csv", "C:\\demo_data_6June2017\\horse_originated_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }


    public static void mainSubjectOf(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_SUBJECT_OF_PERSON-out.csv", "C:\\demo_data_6June2017\\subject_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }

    public static void mainRegisteredIn(String[] args) {
        splitFileToChunks("C:\\demo_data_6June2017\\kingdomsRelations_REGISTERED_GUILD-out.csv", "C:\\demo_data_6June2017\\registered_chunks",
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .build(),
                "startDate");
    }
    public static void main(String[] args) {
        mainOwnHorses(args);
    }
}
