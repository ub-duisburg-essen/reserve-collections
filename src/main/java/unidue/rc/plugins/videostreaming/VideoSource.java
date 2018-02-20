package unidue.rc.plugins.videostreaming;

import java.util.List;

public class VideoSource {

    private final String id;

    public VideoSource(final String id) {
        this.id = id;
    }

    private String scheme;

    private String host;

    private int port;

    private String path;

    private String pathSuffix;

    private String type;

    private boolean isSecured;

    private List<String> extensions;

    public String getId() {
        return id;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getPathSuffix() {
        return pathSuffix;
    }

    public void setPathSuffix(final String pathSuffix) {
        this.pathSuffix = pathSuffix;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public boolean isSecured() {
        return isSecured;
    }

    public void setSecured(final boolean secured) {
        isSecured = secured;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(final List<String> extensions) {
        this.extensions = extensions;
    }
}
