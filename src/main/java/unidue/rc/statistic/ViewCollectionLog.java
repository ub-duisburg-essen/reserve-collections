package unidue.rc.statistic;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
