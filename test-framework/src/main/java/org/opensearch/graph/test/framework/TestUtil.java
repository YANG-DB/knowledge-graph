package org.opensearch.graph.test.framework;



import java.io.File;

public class TestUtil {
    public static void deleteFolder(String folder) {
        File folderFile = new File(folder);
        File[] files = folderFile.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f.getAbsolutePath());
                } else {
                    f.delete();
                }
            }
        }
        folderFile.delete();
    }
}
