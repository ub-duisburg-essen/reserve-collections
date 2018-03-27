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
package unidue.rc.workflow;

import org.apache.cayenne.di.Inject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ReserveCollection;
import unidue.rc.system.BaseCronJob;

import java.util.List;

public class ResetProlongCronJobImpl extends BaseCronJob implements ResetProlongCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(ResetProlongCronJobImpl.class);

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private CollectionService collectionService;

    @Override
    protected void run(final JobExecutionContext context) throws JobExecutionException {

        LOG.info("reset prolong on reserve collections");

        resetProlong(0, BaseDAO.MAX_RESULTS);

        LOG.info("...reset of prolong finished");
    }

    private void resetProlong(final int offset, final int maxResults) {

        List<ReserveCollection> collections = collectionDAO.getProlongedCollections(offset, maxResults);

        for (ReserveCollection collection : collections) {

            collection.setProlongUsed(null);
            collection.setProlongCode(collectionService.generateProlongCode());
            try {
                collectionService.update(collection);
            } catch (CommitException e) {
                LOG.warn("could not update prolong data of collection " + collection.getId());
            }
        }
        if (collections.size() >= maxResults) {
            resetProlong(offset + maxResults, maxResults);
        }
    }
}
