package unidue.rc.migration;

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

import miless.model.Files;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MigrationDAO;
import unidue.rc.model.Migration;
import unidue.rc.system.BaseCronJob;
import unidue.rc.system.SystemConfigurationService;

import java.io.File;
import java.util.List;

/**
 * Created by nils on 13.07.15.
 */
public class MigrationCodeCronJobImpl extends BaseCronJob implements MigrationCodeCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationCodeCronJobImpl.class);

    private static final int MAX_QUERY_RESULTS = 200;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private MigrationDAO migrationDAO;

    @Inject
    private LegacyXMLService xmlService;

    private File fileStorageRoot;

    @Override
    protected void run(JobExecutionContext executionContext) {

        LOG.info("running migration code generation");
        fileStorageRoot = new File(config.getString("legacy.file.storage.path"));

        long count = getIndexMSAFileCount();
        LOG.info("total count of index.msa files: " + count);

        int offset = 0;
        while (offset < count) {

            ObjectContext objectContext = BaseContext.getThreadObjectContext();

            SelectQuery query = new SelectQuery(Files.class);
            query.setQualifier(ExpressionFactory.matchExp(Files.PATH_PROPERTY, "index.msa"));
            query.addOrdering(Files.ID_PROPERTY, SortOrder.ASCENDING);
            query.setFetchLimit(MAX_QUERY_RESULTS);
            query.setFetchOffset(offset);

            List<Files> indexMSAFiles = objectContext.performQuery(query);
            for (Files f : indexMSAFiles) {
                File indexFile = new File(fileStorageRoot, f.getStorageid());
                putCode(indexFile);
            }
            offset += MAX_QUERY_RESULTS;
        }

        LOG.info("...migration code generation finished");
    }

    private long getIndexMSAFileCount() {

        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        SQLTemplate query = new SQLTemplate(Files.class, "select count(*) as total from files where path = 'index.msa'");
        query.setFetchingDataRows(true);
        List<DataRow> count = objectContext.performQuery(query);
        Long indexMSAFilesCount = (Long) count.get(0).get("total");
        return indexMSAFilesCount;
    }

    private void putCode(File indexFile) {
        try {
            // check if code is present
            String code = xmlService.getMigrationCode(indexFile);
            if (StringUtils.isEmpty(code)) {
                code = RandomStringUtils.random(16, true, true);
                xmlService.setMigrationCode(indexFile, code);
            }
            // check migration object in db
            String documentID = xmlService.getDocumentID(indexFile.getAbsolutePath());
            Migration migration = migrationDAO.getMigrationByDocID(documentID);
            try {
                // if no migration was found create a new one
                if (migration == null) {
                    migration = new Migration();
                    migration.setDocumentID(Integer.valueOf(documentID));
                    migration.setMigrationCode(code);
                    migrationDAO.create(migration);
                } else if (!migration.getMigrationCode().equals(code)) {
                    // else update existing one with the code of the xml
                    migration.setMigrationCode(code);
                    migrationDAO.update(migration);
                }
            } catch (CommitException e) {
                LOG.error("could not set migration code to file " + indexFile.getAbsolutePath(), e);
            }
        } catch (MigrationException e) {
            LOG.error("could not set migration code to file " + indexFile.getAbsolutePath(), e);
        }
    }
}
