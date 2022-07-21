package org.opensearch.graph.services.appRegistrars;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.FuseError;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.results.CsvQueryResult;
import org.opensearch.graph.model.results.TextContent;
import org.opensearch.graph.model.transport.ContentResponse;
import org.jooby.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistrarsUtils {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TEXT_CSV = "text/csv";
    public static final String IMAGE_SVG_XML = "image/svg+xml";
    //json fast serializer
    public static ObjectMapper mapper = new ObjectMapper();

    /**
     * result is projected according to mime type
     *
     * @param req
     * @param res
     * @param response
     * @return
     * @throws Throwable
     */
    public static void with(Request req, Response res, ContentResponse<Object> response) throws Throwable {
        if (response.getData() instanceof FuseError) {
            //return error (log)
            res.status(Status.SERVER_ERROR);
            res.type(MediaType.json);
            res.send(response);
        }

        //write content as temp file
        if (req.accepts(APPLICATION_OCTET_STREAM).isPresent()) {
            res.type(MediaType.octetstream);
            File tempFile = File.createTempFile(response.getRequestId(), "-suffix");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(response.getData().toString());
            writer.close();
            tempFile.deleteOnExit();
            res.status(response.status().getStatus());
            //has to be last
            res.download(tempFile);
        } else if (req.accepts(TEXT_CSV).isPresent()) {
            res.type(MediaType.text);
            String now = Instant.now().toString();
            File tempFile = File.createTempFile("csv_" + now, ".csv");

            QueryResourceInfo queryResourceInfo = (QueryResourceInfo) response.getData();
            if (!queryResourceInfo.getCursorResourceInfos().isEmpty() && !queryResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty()) {
                //get only the data content from the page resource
                Object element = ((queryResourceInfo).getCursorResourceInfos().get(0)).getPageResourceInfos().get(0).getData();
                String content = element.toString();
                if (element instanceof TextContent) {
                    content = ((TextContent) element).content();
                }
                Files.write(tempFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                tempFile.deleteOnExit();
                res.status(response.status().getStatus());
                //has to be last
                res.download(tempFile);
            }
        } else if (req.accepts(APPLICATION_JSON).isPresent()) {
            res.type(MediaType.json);
            res.status(response.status().getStatus());
        } else if (req.accepts(IMAGE_SVG_XML).isPresent()) {
            ((File) response.getData()).deleteOnExit();
            res.status(response.status().getStatus());
            //has to be last
            res.download((File) response.getData());
        }

        //default response
        res.status(response.status().getStatus());
        res.type(MediaType.json);
        res.send(response);
    }

    protected static Result withImg(Request req, Response res, ContentResponse<File> response) throws Throwable {
        //write content as temp file
        if (req.accepts(IMAGE_SVG_XML).isPresent()) {
            res.download(response.getData());
            res.status(response.status().getStatus());
            response.getData().deleteOnExit();
        }
        return Results.with(response, response.status().getStatus());
    }


}
