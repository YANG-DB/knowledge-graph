package org.opensearch.graph.model.resourceInfo;




import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by lior.perry on 6/11/2017.
 */
public class FuseError {
    private String errorCode;
    private String errorDescription;

    public FuseError() {}

    public FuseError(String errorCode, Throwable e) {
        this.errorCode = errorCode;
        StringWriter sw = new StringWriter();
        if(e!=null) {
            e.printStackTrace(new PrintWriter(sw));
            //todo check is in debug mode
            e.printStackTrace();
            this.errorDescription = e.getMessage() != null ? e.getMessage() : sw.toString();
        }
    }

    public FuseError(String errorCode, String errorDescription) {
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
        return "FuseError{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }


    public static class FuseErrorException extends RuntimeException {
        private final FuseError error;

        public FuseErrorException(String message, FuseError error) {
            super(message);
            this.error = error;
        }

        public FuseErrorException(String error,String description) {
            super();
            this.error = new FuseError(error,description);
        }

        public FuseErrorException(FuseError error) {
            super();
            this.error = error;
        }

        public FuseErrorException(String message, Throwable cause, FuseError error) {
            super(message, cause);
            this.error = error;
        }

        public FuseErrorException(String message, Throwable cause) {
            super(message, cause);
            this.error = new FuseError(message,cause);
        }

        public FuseError getError() {
            return error;
        }

        @Override
        public String toString() {
            return "FuseErrorException{" +
                    "error=" + error +
                    '}';
        }
    }
}
