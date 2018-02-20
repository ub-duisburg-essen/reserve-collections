package unidue.rc.plugins.videostreaming;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import unidue.rc.system.SystemConfigurationService;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WowzaStreamingService implements VideoStreamingService {

    @Inject
    private Logger log;

    @Inject
    private SystemConfigurationService config;

    private ArrayList<VideoSource> streamSources;

    private void initVideoSource(final String streamSource) {

        VideoSource source = new VideoSource(streamSource);
        source.setScheme(this.config.getString(buildPropertyPath(streamSource, "scheme"),
                config.getString("server.protocol")));
        source.setHost(this.config.getString(buildPropertyPath(streamSource, "host"),
                config.getString("server.name")));
        source.setPort(this.config.getInt(buildPropertyPath(streamSource, "port"),
                config.getInt("server.port", 0)));
        source.setPath(this.config.getString(buildPropertyPath(streamSource, "path")));
        source.setPathSuffix(this.config.getString(buildPropertyPath(streamSource, "path.suffix")));
        source.setType(this.config.getString(buildPropertyPath(streamSource, "type")));
        source.setSecured(this.config.getBoolean(buildPropertyPath(streamSource, "secured"),
                false));
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

        config.getStringArray("video.streaming.enabled")
                .forEach(this::initVideoSource);
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
        if (StringUtils.isNotBlank(source.getPathSuffix()))
            uriBuilder.setPath(uriBuilder.getPath() + source.getPathSuffix());

        uriBuilder.setParameters(queryParams);

        if (source.isSecured()) {
            addSecurityParameters(uriBuilder, source);
        }

        return uriBuilder.build();
    }

    private void addSecurityParameters(final URIBuilder uriBuilder, final VideoSource source) {
        StringBuilder hashBuilder = new StringBuilder();

        /*
        Start the string with the content path to the streaming asset (live stream name or VOD file name).
        The content path is the part of the URL that starts with the application name
        (excluding the '/' that precedes the application name) and continues through to the
        end of the stream name or file name. Be sure to exclude all HTTP request keywords after the
        stream name or file name (for example, /manifest.m3u8...).
         */
        int pathStartIdx = StringUtils.startsWith(uriBuilder.getPath(), "/")
                           ? 1
                           : 0;
        int pathEndIdx = uriBuilder.getPath()
                .length();
        pathEndIdx -= StringUtils.isNotBlank(source.getPathSuffix())
                      ? source.getPathSuffix()
                              .length()
                      : 0;

        hashBuilder.append(uriBuilder.getPath()
                .substring(pathStartIdx, pathEndIdx));

        /*
        Append the '?' character to the path that you created in the previous step. This character
        separates the content path from the public SecureToken query parameters that follow.
         */
        hashBuilder.append("?");

        /*
        Append the public SecureToken query parameters, shared secret, and client IP address
        (if applicable) to the '?' character that you created in the previous step.
        These items MUST be in alphabetical order and separated by the '&' character.
         */
        List<String> params = new ArrayList<>();

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startTime = now;
        ZonedDateTime endTime = now.plusHours(2);

        // hash query parameters
        String paramPrefix = config.getString(buildPropertyPath(source.getId(), "param.prefix"));
        params.add(config.getString(buildPropertyPath(source.getId(), "secret")));
        params.add(paramPrefix + "endtime=" + endTime.toEpochSecond());
        params.add(paramPrefix + "starttime=" + startTime.toEpochSecond());
        params.sort(String::compareTo);
        hashBuilder.append(String.join("&", params));

        // build the hash
        String digestMethod = config.getString(buildPropertyPath(source.getId(), "hash.digest"));
        try {
            MessageDigest digest = MessageDigest.getInstance(digestMethod);
            log.debug("hash source: " + hashBuilder.toString());
            byte[] bytes = digest.digest(hashBuilder.toString()
                    .getBytes(Charset.forName("UTF-8")));
            String hash = Base64.getUrlEncoder().encodeToString(bytes);
            log.debug("       hash: " + hash);
            uriBuilder.addParameter(paramPrefix + "hash", hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("could not create MessageDigest for " + digestMethod, e);
        }

        // start time
        uriBuilder.addParameter(paramPrefix + "starttime", Long.toString(startTime.toEpochSecond()));

        // end time
        uriBuilder.addParameter(paramPrefix + "endtime", Long.toString(endTime.toEpochSecond()));

        log.debug("   uri: " + uriBuilder.toString());
    }
}
