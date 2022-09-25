package org.opensearch.graph.model.resourceInfo;

/*-
 * #%L
 * opengraph-model
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






import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by lior.perry on 6/11/2017.
 */
public class GraphError {
    private String errorCode;
    private String errorDescription;

    public GraphError() {}

    public GraphError(String errorCode, Throwable e) {
        this.errorCode = errorCode;
        StringWriter sw = new StringWriter();
        if(e!=null) {
            e.printStackTrace(new PrintWriter(sw));
            //todo check is in debug mode
            e.printStackTrace();
            this.errorDescription = e.getMessage() != null ? e.getMessage() : sw.toString();
        }
    }

    public GraphError(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    @Override
    public String toString() {
        return "GraphError{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }


    public static class GraphErrorException extends RuntimeException {
        private final GraphError error;

        public GraphErrorException(String message, GraphError error) {
            super(message);
            this.error = error;
        }

        public GraphErrorException(String error, String description) {
            super();
            this.error = new GraphError(error,description);
        }

        public GraphErrorException(GraphError error) {
            super();
            this.error = error;
        }

        public GraphErrorException(String message, Throwable cause, GraphError error) {
            super(message, cause);
            this.error = error;
        }

        public GraphErrorException(String message, Throwable cause) {
            super(message, cause);
            this.error = new GraphError(message,cause);
        }

        public GraphError getError() {
            return error;
        }

        @Override
        public String toString() {
            return "GraphErrorException{" +
                    "error=" + error +
                    '}';
        }
    }
}
