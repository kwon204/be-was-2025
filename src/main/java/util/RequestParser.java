package util;

import http.HttpRequest;
import http.constant.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.exception.InvalidRequestLineSyntaxException;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

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

    public static Map<String, String> parseMultipart(byte[] body, String boundary) throws IOException {
        Map<String, String> map = new HashMap<>();
        byte[] boundaryBytes = boundary.getBytes();
        byte[] endBoundaryBytes = (boundary + "--").getBytes();

        logger.debug("parseMultipart start... boundary: {}", boundary);

        int index = 0;
        int len = body.length;

        while(index < len) {
            int nextBoundary = findBoundary(body, boundaryBytes, index);
            if (nextBoundary == -1) {
                break;
            }

            int partStart = index + boundaryBytes.length + 2;
            int partEnd = findBoundary(body, boundaryBytes, partStart);
            if (partEnd == -1) {
                partEnd = findBoundary(body, endBoundaryBytes, partStart);
                if (partEnd == -1) {
                    break;
                }
            }

            int headerEnd = findDCRLF(body, partStart);
            if (headerEnd == -1) {
                break;
            }

            String contentHeader = new String(body, partStart, headerEnd - partStart).trim();
            byte[] partBody = Arrays.copyOfRange(body, headerEnd + 4, partEnd - 2);

            logger.debug("contentHeader: {}", contentHeader);
            logger.debug("from parseMultipart, body: {}", new String(partBody));

            String key = parseContentDisposition(contentHeader, "name");
            String fileName = parseContentDisposition(contentHeader, "filename");
            if (key == null) {
                throw new RuntimeException("multipart name is null");
            }
            if (fileName != null) {
                String newFileName = FileUtils.saveImage(fileName, partBody);
                map.put(key, newFileName);
            } else {
                map.put(key, new String(partBody));
            }

            index = partEnd;
        }
        return map;
    }

    private static int findDCRLF(byte[] body, int start) {
        for (int i = start; i < body.length - 3; i++) {
            if (body[i] == '\r' && body[i + 1] == '\n' && body[i + 2] == '\r' && body[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private static int findBoundary(byte[] body, byte[] boundaryBytes, int start) {
        for (int i = start; i <= body.length - boundaryBytes.length; i++) {
            boolean match = true;
            for (int j = 0; j < boundaryBytes.length; j++) {
                if (body[i + j] != boundaryBytes[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }

    private static String parseContentDisposition(String header, String key) {
        String[] lines = header.split(CRLF);
        for(String line : lines) {
            if (line.toLowerCase().startsWith(HttpHeader.CONTENT_DISPOSITION.value().toLowerCase())) {
                String[] tokens = line.split(";");
                for (String token: tokens) {
                   token = token.trim();
                    if (token.startsWith(key + "=")) {
                        String[] parts = token.split("=");
                        return parts[1].trim().replace("\"", "");
                    }
                }

            }
        }

        return null;
    }

    public static Map<String ,String> parseRequestBody(HttpRequest request) throws IOException {
        if (!request.getHeaders().containsKey(HttpHeader.CONTENT_TYPE.value().toLowerCase())) {
            throw new IllegalArgumentException();
        }
        String fieldValue = request.getHeaders().get(HttpHeader.CONTENT_TYPE.value().toLowerCase());
        String[] tokens = fieldValue.trim().split(";");
        String contentType = tokens[0];

        MimeType mimeType = MimeType.findMimeType(contentType);
        logger.debug("MimeType: {}", mimeType.getMimeType());

        if (mimeType == MimeType.APPLICATION_URLENCODED) {
            return parseBody(new String(request.getBody()));
        } else if (mimeType == MimeType.MULTIPART_FORM_DATA) {
            if (tokens.length < 2) {
                throw new RuntimeException();
            }
            String boundary = tokens[1].split("=")[1].trim();
            boundary = "--" + boundary;
            return parseMultipart(request.getBody(), boundary);
        } else {
            throw new RuntimeException();
        }

    }
}
