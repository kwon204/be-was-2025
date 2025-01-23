package util.exception;

import http.constant.HttpStatus;

public class UnsupportedMimeTypeException extends RuntimeException {
    public final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
    public UnsupportedMimeTypeException(String message) {
        super(message);
    }
}
