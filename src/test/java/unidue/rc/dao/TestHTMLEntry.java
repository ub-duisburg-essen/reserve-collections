package unidue.rc.dao;


import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.Html;
import unidue.rc.model.ReserveCollection;

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 20.02.14 Time: 11:08 To change this template use File | Settings |
 * File Templates.
 */
public class TestHTMLEntry extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestHTMLEntry.class);


    private EntryDAO entryDAO;

    private DbTestUtils dbTestUtils;
    private ReserveCollection rc;

    @BeforeClass
    public void setup() throws Exception {

        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();
            entryDAO = new EntryDAOImpl();
            rc = dbTestUtils.createMockReserveCollection("TestA");
        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @AfterClass
    public void shutdown() {

        dbTestUtils.shutdown();
    }


    @Test
    public void testCreateInvalidHtml() {
        Html html = new Html();
        html.setText(null);
        try {
            entryDAO.createEntry(html, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }


    @Test
    public void testCreateValidHtml() {
        Html html = null;

        try {
            html = dbTestUtils.createMockHtml(rc);
        } catch (CommitException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        html = entryDAO.get(Html.class, html.getId());
        assertTrue(html != null);

    }
}
