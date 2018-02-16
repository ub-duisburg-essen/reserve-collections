package unidue.rc.plugins.videostreaming;

import org.apache.http.NameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface VideoStreamingService {

    List<VideoSource> getVideoSources(String extension);

    URI getSourceURI(VideoSource source, Map<String, String> pathFmt, NameValuePair... queryParams) throws URISyntaxException;
}