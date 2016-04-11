package unidue.rc.statistic;


import unidue.rc.model.AccessLog;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.accesslog.Access;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nils on 14.09.15.
 */
public class ViewCollectionLog implements AccessLogable {

    private static final String VIEW_COLLECTION_RESOURCE_NAME = ReserveCollection.class.getSimpleName();

    private static final Pattern VIEW_COLLECTION_URL_PATTERN = Pattern.compile("(/collection/view/)([0-9]+)");

    @Override
    public boolean matches(Access access) {
        String requestURI = access.getRequestURI();
        return requestURI != null
                && VIEW_COLLECTION_URL_PATTERN.matcher(requestURI).matches();
    }

    @Override
    public AccessLog createAccessLog(Access access) {
        String requestURI = access.getRequestURI();
        Matcher matcher = VIEW_COLLECTION_URL_PATTERN.matcher(requestURI);

        AccessLog log = new AccessLog();
        log.setResource(VIEW_COLLECTION_RESOURCE_NAME);
        log.setRemoteHost(access.getRemoteHost());
        log.setAction(VIEW_ACTION);
        log.setTimestamp(access.getTimestamp().getTime());
        log.setUserAgent(access.getUserAgent());
        if (matcher.find()) {
            String collectionID = matcher.group(2);
            log.setResourceID(Integer.valueOf(collectionID));
        }
        return log;
    }
}
