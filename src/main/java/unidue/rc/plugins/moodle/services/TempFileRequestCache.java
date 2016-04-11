/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.plugins.moodle.services;


import org.apache.log4j.Logger;
import unidue.rc.plugins.moodle.model.ResourceRequest;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * <p>
 * A instance of <code>TempFileRequestCache</code> is ables to bind urls to
 * generated session ids. The urls must first be added to the cache and can be
 * returned by the use of a session id. The urls are only valid for a specific
 * amount of time.
 * </p>
 * <p>
 * Because a cache is an instance of {@link TimerTask} a timer is used schedule
 * the clearage of kept data. Cached urls are thrown away, after the time they
 * can be used expired.
 * </p>
 * Created by nils on 15.06.15.
 */
public class TempFileRequestCache extends TimerTask {
    private static final Logger LOG = Logger.getLogger(TempFileRequestCache.class.getName());

    /**
     * contains the cache itself.
     */
    private Set<CachedData> cache;

    /**
     * {@link Timer} used to schedule this instance.
     */
    private Timer timer;

    /**
     * maximum time in seconds, a url is valid.
     */
    private int maxIdleTimeSec;

    /**
     * {@link SecureRandom} used to generate session ids.
     */
    private SecureRandom random = new SecureRandom();

    /**
     * Creates a new instance of this class with target clear interval in
     * seconds, and maximum time a url should be valid.
     *
     * @param clearIntervalSec  maximum clear interval in seconds
     * @param maxIdleTimeSec maximum idle time in seconds
     */
    public TempFileRequestCache(int clearIntervalSec, int maxIdleTimeSec) {
        this.cache = Collections.synchronizedSet(new HashSet<>());
        this.maxIdleTimeSec = maxIdleTimeSec * 1000;
        this.timer = new Timer();
        this.timer.schedule(this, clearIntervalSec * 1000, clearIntervalSec * 1000);
        LOG.info("created cache with interval of " + clearIntervalSec + " seconds");
    }

    /**
     * Creates a new entry inside this cache.
     *
     * @param request url which should be saved.
     * @return session id which can be used to retrieve given url with
     * {@link TempFileRequestCache#getDataForSid(String)}
     */
    public String addRequest(ResourceRequest request) {

        CachedData cd = new CachedData();
        cd.request = request;

        synchronized (cache) {
            cache.add(cd);
        }

        return cd.sid;
    }

    /**
     * Returns the {@link CachedData} with target sid if it exists,
     * <code>null</code> otherwise.
     *
     * @param sid contains the session id for which the request should be returned
     * @return returns the resource request if one could be found, <code>null</code> otherwise
     */
    public ResourceRequest getDataForSid(String sid) {
        synchronized (cache) {
            Iterator<CachedData> i = cache.iterator();
            while (i.hasNext()) {
                CachedData data = i.next();
                if (data.sid.equals(sid))
                    return data.request;
            }
        }
        return null;
    }

    /**
     * Removes all {@link CachedData} objects from this
     * {@link TempFileRequestCache#cache}, which has expired defined by
     * {@link TempFileRequestCache#maxIdleTimeSec}
     */
    @Override
    public void run() {
        long now = System.currentTimeMillis();
        synchronized (cache) {
            List<CachedData> toRemove = new ArrayList<>();
            Iterator<CachedData> i = cache.iterator();
            while (i.hasNext()) {
                CachedData data = i.next();
                if (now > data.creationTime + maxIdleTimeSec)
                    toRemove.add(data);
            }

            if (toRemove.size() > 0) {

                LOG.debug("removing: " + toRemove);
                cache.removeAll(toRemove);
            }
        }
    }

    class CachedData {
        String sid;

        long creationTime;

        ResourceRequest request;

        Map<Object, String> requestData;

        public CachedData() {
            creationTime = System.currentTimeMillis();
            sid = createSessionId();
            requestData = new HashMap<>();
        }

        private String createSessionId() {
            return new BigInteger(128, random).toString(16);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "CachedData [sid=" + sid + ", creationTime=" + creationTime + ", request=" + request + "]";
        }

    }
}
