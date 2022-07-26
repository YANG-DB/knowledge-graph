package org.opensearch.graph.generator.util;




import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class FileUtils {

    /**
     * zip file
     *
     * @param source
     * @param target
     * @throws IOException
     */
    public static void zip(File source, String target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(source);
        ZipEntry zipEntry = new ZipEntry(source.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
    }
}
