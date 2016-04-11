package unidue.rc.statistic;


import org.apache.commons.lang3.tuple.Pair;
import unidue.rc.model.AccessLog;
import unidue.rc.model.Resource;
import unidue.rc.model.accesslog.Access;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nils on 14.09.15.
 */
public class FileDownloadLog implements AccessLogable {

    private static final String DOWNLOAD_FILE_RESOURCE_NAME = Resource.class.getSimpleName();

    private static final Pair<Pattern, Integer>[] DOWNLOAD_URI_PATTERNS = new Pair[]{
            Pair.of(Pattern.compile("(/collection/view.file:download/)([0-9]+)"), 2),
            Pair.of(Pattern.compile("(/entry/download/)([0-9]+)(/(attachment|inline))"), 2),
            Pair.of(Pattern.compile("(/collection/view.bookchapter:download/)([0-9]+)"), 2),
            Pair.of(Pattern.compile("(/collection/view.journalarticle:download/)([0-9]+)"), 2)
    };

    @Override
    public boolean matches(Access access) {
        String requestURI = access.getRequestURI();

        return requestURI != null
                && Arrays.stream(DOWNLOAD_URI_PATTERNS)
                .filter(pair -> pair.getLeft().matcher(requestURI).matches())
                .findAny()
                .isPresent();
    }

    @Override
    public AccessLog createAccessLog(Access access) {

        String requestURI = access.getRequestURI();
        Optional<Pair<Pattern, Integer>> pattern = Arrays.stream(DOWNLOAD_URI_PATTERNS)
                .filter(pair -> pair.getLeft().matcher(requestURI).matches())
                .findAny();


        AccessLog log = new AccessLog();
        log.setResource(DOWNLOAD_FILE_RESOURCE_NAME);
        log.setRemoteHost(access.getRemoteHost());
        log.setAction(DOWNLOAD_ACTION);
        log.setTimestamp(access.getTimestamp().getTime());
        log.setUserAgent(access.getUserAgent());

        if (pattern.isPresent()) {
            Pair<Pattern, Integer> pair = pattern.get();
            Matcher matcher = pair.getLeft().matcher(requestURI);
            if (matcher.find()) {
                Integer resourceID = Integer.valueOf(matcher.group(pair.getRight()));
                log.setResourceID(resourceID);
            }
        }
        return log;
    }
}
