package unidue.rc.ui.pages.entry;


import com.thoughtworks.selenium.Wait;
import org.apache.cayenne.validation.ValidationException;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.EntryDAOImpl;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * Created by marcus.koesters on 27.02.15.
 */

   public class TestCreateJournal extends SeleniumTestCase {
        private static final Logger LOG = LoggerFactory
                .getLogger(TestCreateJournal.class);

        private DbTestUtils dbTestUtils;
        //private ReserveCollection rc;

        private EntryDAOImpl entryDAO;


        @BeforeClass
        public void setup() throws Exception {
            LOG.info("running " + this.getClass().getName() + " tests");

            try {
                dbTestUtils = new DbTestUtils();
                dbTestUtils.setupdb();

                entryDAO = new EntryDAOImpl();

            } catch (DatabaseException | ValidationException e) {
                LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
                throw e;
            }
        }

    @Test
    public void testCreateJournal() {
        final ReserveCollection rc;
        try {
            rc = dbTestUtils.createMockReserveCollection("TestA");

            open("/entry/createjournal/" + rc.getId());

            new Wait() {

                @Override
                public boolean until() {
                    return isElementPresent("id=articletitle");
                }
            }.wait("articletitle html should be visible", 5000);

            typeInField("articletitle", "Titel");
            typeInField("journaltitle", "JournalTitle");
            typeInField("journalplace", "Duisburg");
            typeInField("articleauthors", "Horst");
            typeInField("journalpagesfrom", "1");
            typeInField("journalpagesto", "2");

            clickAndWait(SUBMIT);
            new Wait() {

                @Override
                public boolean until() {
                    return isElementPresent("id=" + getEntryID(rc));
                }
            }.wait("Journal Entry should be visible", 5000);


        } catch (CommitException e) {
            e.printStackTrace();
        }
    }

    protected void typeInField(final String fieldId, String value) {
        type("id=" + fieldId, value);
    }

    private int getEntryID(ReserveCollection rc) {
        List<Entry> entries = entryDAO.getEntries(rc);
        if (!entries.isEmpty()) {
            return entries.get(0).getId();

        }
        return 0;
    }

    @AfterClass
    public void shutdown() {
        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }


}
