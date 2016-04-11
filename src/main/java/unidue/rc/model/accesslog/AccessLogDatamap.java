package unidue.rc.model.accesslog;


import unidue.rc.model.accesslog.auto._AccessLogDatamap;

public class AccessLogDatamap extends _AccessLogDatamap {

    private static AccessLogDatamap instance;

    private AccessLogDatamap() {}

    public static AccessLogDatamap getInstance() {
        if(instance == null) {
            instance = new AccessLogDatamap();
        }

        return instance;
    }
}
