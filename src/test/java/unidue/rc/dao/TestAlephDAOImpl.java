package unidue.rc.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Book;
import unidue.rc.plugins.alephsync.AlephBook;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by marcus.koesters on 11.11.15.
 */
public class TestAlephDAOImpl implements AlephDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TestAlephDAOImpl.class);

    @Override
    public List<AlephBook> listSignatures(String systemID) throws SQLException {
        LOG.debug("List Signatures called");
        return null;
    }

    @Override
    public void setBookData(Book book, String signature) throws SQLException {
        LOG.debug("Set BookData called");
        book.setTitle("TESTTITEL");
        book.setAuthors("TestAUTOR");
        book.setEdition("Edition");
        book.setPlaceOfPublication("TESTPLACE");
        book.setIsbn("234234234234");
        book.setPublisher("test");
        book.setVolume("123af");

    }
}
