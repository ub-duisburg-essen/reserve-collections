package unidue.rc.plugins.videostreaming;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.system.SystemConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WowzaStreamingService implements VideoStreamingService {

    @Inject
    private SystemConfigurationService config;

    private ArrayList<VideoSource> streamSources;

    private void initVideoSource(final String streamSource) {

        VideoSource source = new VideoSource();
        source.setScheme(this.config.getString(buildPropertyPath(streamSource, "scheme"), config.getString("server.protocol")));
        source.setHost(this.config.getString(buildPropertyPath(streamSource, "host"), config.getString("server.name")));
        source.setPort(this.config.getInt(buildPropertyPath(streamSource, "port"), config.getInt("server.port", 0)));
        source.setPath(this.config.getString(buildPropertyPath(streamSource, "path")));
        source.setType(this.config.getString(buildPropertyPath(streamSource, "type")));
        source.setExtensions(this.config.getStringArray(buildPropertyPath(streamSource, "extensions")));

        this.streamSources.add(source);
    }

    private String buildPropertyPath(String streamSource, String property) {
        return String.format("video.streaming.sources.%s.%s", streamSource, property);
    }

    @Override
    public List<VideoSource> getVideoSources(String extension) {
        if (streamSources == null)
            initVideoSources();

        return streamSources.stream()
                .filter(source -> source.getExtensions()
                        .contains(extension))
                .collect(Collectors.toList());
    }

    private void initVideoSources() {

        this.streamSources = new ArrayList<>();

        config.getStringArray("video.streaming.enabled").forEach(this::initVideoSource);
    }

    @Override
    public URI getSourceURI(final VideoSource source, Map<String, String> pathFmt, NameValuePair... queryParams) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder();

        if (StringUtils.isNotBlank(source.getScheme()))
            uriBuilder.setScheme(source.getScheme());
        if (StringUtils.isNotBlank(source.getHost()))
            uriBuilder.setHost(source.getHost());
        if (source.getPort() > 0)
            uriBuilder.setPort(source.getPort());
        if (StringUtils.isNotBlank(source.getPath()))
            uriBuilder.setPath(StrSubstitutor.replace(source.getPath(), pathFmt));
        uriBuilder.setParameters(queryParams);

        return uriBuilder.build();
    }
}
