package unidue.rc.statistic;


import unidue.rc.model.AccessLog;
import unidue.rc.model.accesslog.Access;

/**
 * An <code>AccessLogable</code> can be used to match {@link Access} objects from web server logs and use the
 * as aggregated {@link AccessLog} data.
 */
public interface AccessLogable {

    String VIEW_ACTION = "view";
    String DOWNLOAD_ACTION = "download";

    /**
     * Returns <code>true</code> if target {@link Access} matches specific rules to aggregate statistic data,
     * <code>false</code> otherwise.
     *
     * @param access access object that may matches this loggable
     * @return <code>true</code> is given access matches
     * @see #createAccessLog(Access)
     */
    boolean matches(Access access);

    /**
     * Creates a new {@link AccessLog} object that can be used as aggregated statistic data. Target access must be
     * matches by this matches method.
     *
     * @param access access object that may matches this loggable
     * @return <code>true</code> is given access matches
     * @see #matches(Access)
     */
    AccessLog createAccessLog(Access access);
}
