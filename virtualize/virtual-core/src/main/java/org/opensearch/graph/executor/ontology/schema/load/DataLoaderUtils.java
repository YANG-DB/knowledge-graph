package org.opensearch.graph.executor.ontology.schema.load;

/*-
 * #%L
 * virtual-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.opensearch.graph.executor.ontology.schema.RawSchema;
import org.opensearch.graph.model.date.DateParser;
import org.opensearch.graph.model.resourceInfo.GraphError;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geojson.Point;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.Client;
import org.opensearch.common.xcontent.XContentType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.zip.*;

import static java.lang.Double.parseDouble;

public interface DataLoaderUtils {
    DateParser parser = DateParser.newBuilder().build();

    static long init(Client client, RawSchema schema) {
        try {
            String workingDir = System.getProperty("user.dir");
            File templates = Paths.get(workingDir, "indexTemplates").toFile();
            File[] templateFiles = templates.listFiles();
            if (templateFiles != null) {
                for (File templateFile : templateFiles) {
                    String templateName = FilenameUtils.getBaseName(templateFile.getName());
                    String template = FileUtils.readFileToString(templateFile, "utf-8");
                    if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                        final AcknowledgedResponse acknowledgedResponse = client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet(1500);
                        if (!acknowledgedResponse.isAcknowledged()) return -1;
                    }
                    final AcknowledgedResponse acknowledgedResponse = client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet(1500);
                    if (!acknowledgedResponse.isAcknowledged()) return -1;
                }
            }

            Iterable<String> allIndices = schema.indices();

            Stream.ofAll(allIndices)
                    .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                    .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
            Stream.ofAll(allIndices).forEach(index -> client.admin().indices()
                    .create(new CreateIndexRequest(index)).actionGet());

            return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
        } catch (Throwable t) {
            throw new GraphError.GraphErrorException("INIT() - Create Indices error ", t);

        }
    }

    /**
     * drop all indices existing for this ontology
     *
     * @param client
     * @param schema
     * @return
     */
    static int drop(Client client, RawSchema schema) {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());

    }


    /**
     * check is zip file
     *
     * @param file
     * @return
     * @throws IOException
     */
    static String getZipType(java.io.File file) {
        try (ZipInputStream zipFile = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            if (zipFile.available() == 1 && zipFile.getNextEntry() != null) {
                zipFile.close();
                return "application/zip";
            }
        } catch (IOException err1) {
            try (final GZIPInputStream stream = new GZIPInputStream(Files.newInputStream(file.toPath()))) {
                if (stream.available() == 1) {
                    stream.close();
                    return "application/gzip";
                }
            } catch (IOException err2) {
                return "";
            }
        }
        return "";
    }

    /**
     * @param zipIn
     * @return unzipped File
     * @throws IOException
     */
    static List<File> extractFile(File zipIn) throws IOException {
        List<File> files = new ArrayList<>();
        try (java.util.zip.ZipFile zipFile = new ZipFile(zipIn)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(zipIn.getParent(), entry.getName());
                files.add(entryDestination);
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
        return files;
    }

    static ByteArrayOutputStream extractFile(InflaterInputStream zipIn) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return stream;
    }

    static Object parseValue(String explicitType, Object value, DateFormat sdf) {
        switch (explicitType) {
            case "text":
            case "string":
            case "stringValue":
                return value.toString();
            case "int":
            case "intValue":
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    try {
                        return Long.valueOf(value.toString());
                    } catch (NumberFormatException e1) {
                        return value.toString();
                    }
                }
            case "long":
            case "longValue":
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "float":
            case "floatValue":
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "date":
            case "dateValue":
                try {
                    return sdf.format(sdf.parse(value.toString()));
                } catch (ParseException e) {
                    try {
                        return sdf.format(new Date(value.toString()));
                    } catch (Throwable e1) {
                        try {
                            return sdf.format(parser.parseDate(value.toString()));
                        } catch (Throwable e2) {
                            try {
                                long time = Long.parseLong(value.toString());
                                return new Date(time);
                            } catch (Throwable ignore) {
                                return value.toString();
                            }
                        }
                    }
                }
            case "geo":
            case "geoValue":
                return new Point(
                        Double.parseDouble(value.toString().split("[,]")[1]),
                        Double.parseDouble(value.toString().split("[,]")[0]));
        }
        return value;
    }

    static boolean validateValue(String explicitType, Object value, DateFormat sdf) {
        switch (explicitType) {
            case "text":
            case "string":
            case "stringValue":
                return Objects.nonNull(value);
            case "int":
            case "intValue":
                try {
                    Integer.valueOf(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    try {
                        Long.valueOf(value.toString());
                        return true;
                    } catch (NumberFormatException e1) {
                        return false;
                    }
                }
            case "long":
            case "longValue":
                try {
                    Long.valueOf(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "float":
            case "floatValue":
                try {
                    Float.valueOf(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "date":
            case "dateValue":
                try {
                    sdf.parse(value.toString());
                    return true;
                } catch (ParseException e) {
                    try {
                        sdf.format(new Date(value.toString()));
                        return true;
                    } catch (Throwable e1) {
                        try {
                            parser.parseDate(value.toString());
                            return true;
                        } catch (Throwable err) {
                            try {
                                long time = Long.parseLong(value.toString());
                                new Date(time);
                                return true;
                            } catch (Throwable ignore) {
                                return false;
                            }
                        }
                    }

                }
            case "geo":
            case "geoValue":
                try {
                    new Point(
                            parseDouble(value.toString().split("[,]")[1]),
                            parseDouble(value.toString().split("[,]")[0]));
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
        }
        return false;
    }


}
