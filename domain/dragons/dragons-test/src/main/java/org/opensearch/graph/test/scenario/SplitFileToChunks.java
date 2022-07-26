package org.opensearch.graph.test.scenario;



import org.opensearch.graph.test.etl.ChunkPartitioner;
import org.opensearch.graph.test.etl.Partitioner;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Roman on 06/06/2017.
 */
public class SplitFileToChunks {
    public static void main(String[] args) {
        String filePath = "E:\\fuse_data\\edgesAfterEtl\\dragons.csv";
        String destFolder = "E:\\fuse_data\\demo_data_6June2017\\chunks";

        new File(destFolder).mkdirs();

        int chunkSize = 100000; // number of lines
        Partitioner partitioner = new ChunkPartitioner(chunkSize);

        Map<String, List<String>> bufferedPartitions = new HashMap<>();
        int maxBufferedLines = 100000;

        int numLines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String partitionKey = partitioner.getPartition(null);

                List<String> bufferedPartition = bufferedPartitions.get(partitionKey);
                if (bufferedPartition == null) {
                    bufferedPartition = new ArrayList<>();
                    bufferedPartitions.put(partitionKey, bufferedPartition);
                }

                bufferedPartition.add(line);
                numLines++;

                if (numLines == maxBufferedLines) {
                    flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
                    numLines = 0;
                    bufferedPartitions.clear();
                }
            }

            flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flushBufferedPartitions(Map<String, List<String>> bufferedPartitions, String filePath, String destFolder) throws IOException {
        String fileName = FilenameUtils.removeExtension(FilenameUtils.getName(filePath));
        String fileExtension = FilenameUtils.getExtension(filePath);

        for(Map.Entry<String, List<String>> entry : bufferedPartitions.entrySet()) {
            String partitionFileName = getPartitionFileName(destFolder, fileName, entry.getKey(), fileExtension);
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(partitionFileName))) {
                for (String line : entry.getValue()) {
                    wr.write(line + System.lineSeparator());
                }
            }
        }
    }

    private static String getPartitionFileName(String destFolder, String fileName, String partitionKey, String fileExtension) {
        return Paths.get(destFolder, fileName + "." + partitionKey + "." + fileExtension).toString();
    }
}
