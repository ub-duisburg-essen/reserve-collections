package unidue.rc.plugins.videostreaming;

import java.util.List;

public class VideoSource {

    private String scheme;

    private String host;

    private int port;

    private String path;

    private String type;

    private List<String> extensions;

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

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(final List<String> extensions) {
        this.extensions = extensions;
    }
}
