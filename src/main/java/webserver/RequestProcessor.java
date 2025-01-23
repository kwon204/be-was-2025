package webserver;

import http.*;
import http.constant.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestParser;
import util.exception.InvalidRequestLineSyntaxException;
import util.exception.UnsupportedMimeTypeException;

import java.io.*;

public class RequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);
    public RequestProcessor() {
    }

    public void process(InputStream in, DataOutputStream dos) throws IOException {
        try{
            HttpRequest httpRequest = RequestParser.parse(in);

            HttpResponse httpResponse = new HttpResponse(dos);

            HttpRequestDispatcher httpRequestDispatcher = new HttpRequestDispatcher(httpRequest, httpResponse);
            httpRequestDispatcher.dispatch();
        } catch (InvalidRequestLineSyntaxException e) {
            errorResponse(dos, e.httpStatus, e);
        } catch (IOException e) {
            errorResponse(dos, HttpStatus.INTERNAL_SERVER_ERROR, e);
        } catch (IllegalArgumentException e) {
            errorResponse(dos, HttpStatus.BAD_REQUEST, e);
        } catch (UnsupportedMimeTypeException e) {
            errorResponse(dos, e.httpStatus, e);
        }
    }



    private void errorResponse(DataOutputStream dos, HttpStatus httpStatus, Exception e) throws IOException {
        logger.error(e.getMessage());
        HttpResponse response = new HttpResponse(dos);
        response.sendError(httpStatus, e.getMessage());
    }
}