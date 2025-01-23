package util;

public enum MimeType {
    HTML("html", "text/html; charset=UTF-8"),
    CSS("css", "text/css; charset=UTF-8"),
    JS("js", "text/js; charset=UTF-8"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpg"),
    JPEG("jpeg", "image/jpeg"),
    ICO("ico", "image/x-icon"),
    SVG("svg", "image/svg+xml"),
    TXT("txt", "text/plain; charset=UTF-8"),

    MULTIPART_FORM_DATA("", "multipart/form-data"),
    APPLICATION_URLENCODED("", "application/x-www-form-urlencoded"),
    APPLICATION_OCTET_STREAM("", "application/octet-stream");

    private final String extension;
    private final String mimeType;

    MimeType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public static MimeType findMimeType(String contentType) {
        for(MimeType type : MimeType.values()) {
            if (type.getMimeType().toLowerCase().equals(contentType)) {
                return type;
            }
        }
        return MimeType.APPLICATION_OCTET_STREAM;
    }
}
