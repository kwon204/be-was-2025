package http;

import handler.Handler;
import http.constant.HttpMethod;
import http.constant.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ApiPathPool;
import util.StaticResourcePathPool;
import util.exception.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpRequestDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestDispatcher.class);
    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    public HttpRequestDispatcher(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public void dispatch() throws IOException {
        try {
            String path = httpRequest.getPath().toLowerCase();
            HttpMethod method = httpRequest.getMethod();

            if (StaticResourcePathPool.getInstance().isAvailable(method, path)) {
                Handler handler = StaticResourcePathPool.getInstance().getHandler(path);
                handler.handle(httpRequest, httpResponse);
                return;
            }

            if (!ApiPathPool.getInstance().isAvailable(method, path)) {
                throw new NoSuchPathException("해당 경로를 찾을 수 없습니다.");
            }

            Method classMethod = ApiPathPool.getInstance().getMethod(method, path);
            classMethod.invoke(ApiPathPool.getInstance().getClass(path), httpRequest, httpResponse);
        } catch (NoSuchPathException e) {
            httpResponse.sendError(e.httpStatus, e.getMessage());
        } catch (NotAllowedMethodException e) {
            httpResponse.sendError(e.httpStatus, e.getMessage());
        } catch (IllegalAccessException e) {
            httpResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (UserNotFoundException e) {
            httpResponse.sendError(e.httpStatus, e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error(e.getCause().getMessage());
            if (e.getCause() instanceof SessionNotFoundException ex) {
                httpResponse.sendError(ex.httpStatus, ex.getMessage());
            } else if (e.getCause() instanceof UserNotFoundException ex) {
                httpResponse.sendError(ex.httpStatus, ex.getMessage());
            } else if (e.getCause() instanceof ArticleNotFoundException ex) {
                httpResponse.sendError(ex.httpStatus, ex.getMessage());
            } else {
                httpResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }
}
