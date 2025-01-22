package http;

import http.constant.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.RequestParser;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String,String> queries;
    private final Map<String, String> headers;
    private final byte[] body;

    private final URI uri;

    private final Map<String, String> sessionIds;

    public HttpRequest(String method, String path, String version, Map<String, String> headers, byte[] body) throws UnsupportedEncodingException {
        this.method = HttpMethod.valueOf(method);
        this.version = version;

        this.uri = URI.create(path);
        this.path = uri.getPath();

        this.headers = headers;
        this.body = body;

        this.queries = RequestParser.parseQuery(uri.getQuery());
        this.sessionIds = RequestParser.extractSessionIds(this.headers);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getQueries() {
        return queries;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public URI getUri() {
        return uri;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, String> getSessionIds() {
        return sessionIds;
    }
}
