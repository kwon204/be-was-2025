package util.exception;

import http.constant.HttpStatus;

public class InternalServerException extends RuntimeException {
    public final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    public InternalServerException(String message) {
        super(message);
    }
}
