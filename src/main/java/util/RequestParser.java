package util;

import http.HttpRequest;
import http.constant.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.exception.InvalidRequestLineSyntaxException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);
    private static final String CRLF = "\r\n";
    private static final String DCRLF = "\r\n\r\n";

    public static HttpRequest parse(InputStream in) throws IOException {
        String method;
        String path;
        String version;
        Map<String, String> headers;
        byte[] body = null;

        DataInputStream dis = new DataInputStream(in);
        StringBuilder sb = new StringBuilder();
        byte i;

        while((i = dis.readByte()) != -1) {
            char c = (char) i;
            sb.append(c);
            if (sb.lastIndexOf(DCRLF) != -1) break;
            if (dis.available() == 0) break;
        }

        String header = sb.toString().trim();

        logger.debug("Header is : \n{}", header);

        String[] tokens = header.split(CRLF);
        List<String> headerList = List.of(tokens);

        String[] requestLine = resolveRequestLine(headerList.get(0));

        method = requestLine[0];
        path = requestLine[1];
        version = requestLine[2];

        headers = parseHeaders(headerList.subList(1, headerList.size()));

        if (headers.containsKey(HttpHeader.CONTENT_LENGTH.value().toLowerCase())) {
            int len = Integer.parseInt(headers.getOrDefault(HttpHeader.CONTENT_LENGTH.value().toLowerCase(), "0"));

            body = new byte[len];
            int flag = dis.readNBytes(body, 0, len);

            if (flag != len) {
                logger.debug("actual read byte: {}, expteced read byte : {}", flag, len);
            }

            logger.debug("body: {}", new String(body));
        }

        return new HttpRequest(method, path, version, headers, body);
    }

    private static Map<String, String> parseHeaders(List<String> request) {
        Map<String, String> headers = new HashMap<>();
        for (String header: request) {
            if (header.isBlank()) break;
            String[] tokens = header.split(":", 2);
            headers.merge(tokens[0].trim().toLowerCase(), tokens[1].trim(), String::concat);
        }

        return headers;
    }

    public static Map<String, String> parseQuery(String queryString) throws UnsupportedEncodingException {
        Map<String, String> query = new HashMap<>();
        if (queryString == null) {
            return query;
        }

        return RequestParser.parseBody(queryString);
    }

    public static Map<String, String> extractSessionIds(Map<String ,String> headers) {
        Map<String, String> ids = new HashMap<>();
        if (!headers.containsKey(HttpHeader.COOKIE.value().toLowerCase())) {
            return ids;
        }

        return Cookie.parse(headers.get(HttpHeader.COOKIE.value().toLowerCase()));
    }

    private static String[] resolveRequestLine(String requestLine) {
        String[] tokens =  requestLine.split(" ");

        if (tokens.length != 3) {
            throw new InvalidRequestLineSyntaxException("");
        }

        return tokens;
    }

    public static Map<String, String> parseBody(String body) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();

        body = URLDecoder.decode(body, "utf-8");

        String[] tokens = body.split("&");
        for(String token: tokens) {
            String[] items = token.split("=");
            String key = items[0].trim();
            String value = items.length > 1 ? items[1].trim() : null;
            map.put(key, value);
        }

        return map;
    }
}
