package unidue.rc.dao;


import unidue.rc.model.Book;
import unidue.rc.plugins.alephsync.AlephBook;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nils on 06.10.15.
 */
public interface AlephDAO {

    String SERVICE_NAME = "AlephDAO";

    /**
     * Retrieves the signatures of all loans by the user with the given systemID
     *
     * @param systemID alephs system id of a book
     * @return {@link List} with all available aleph books
     * @throws SQLException if an error occured during aleph db access
     **/
    List<AlephBook> listSignatures(String systemID) throws SQLException;

    /**
     * Retrieves all information of aleph by target signature and sets found information into given book.
     *
     * @param book      object whose data should be set
     * @param signature signature of the book
     * @throws SQLException if an error occured during aleph db access
     */
    void setBookData(Book book, String signature) throws SQLException;
}
