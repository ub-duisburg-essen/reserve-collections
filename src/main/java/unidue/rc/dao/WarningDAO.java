package unidue.rc.dao;


import miless.model.User;
import unidue.rc.model.Mail;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Warning;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by nils on 17.03.16.
 */
public interface WarningDAO extends BaseDAO {

    String SERVICE_NAME = "WarningDAO";

    /**
     * Creates a new {@link Warning} with given parameters.
     *
     * @param mail          {@link Mail} that was send to the user
     * @param collection    {@link ReserveCollection} the mail was send to
     * @param user          {@link User} who has be addressed
     * @param calculatedFor the calculated date to the warning
     * @return the created warning object
     * @throws CommitException thrown if the warning could not be created
     */
    Warning create(Mail mail, ReserveCollection collection, User user, LocalDate calculatedFor) throws CommitException;

    /**
     * Returns a list of all warnings that were send to a {@link User} who is or was addressed to target
     * {@link ReserveCollection}
     *
     * @param userID     user id of the warning
     * @param collection collection to the warning
     * @return a list with all warning or an empty list
     */
    List<Warning> getWarnings(Integer userID, ReserveCollection collection);
}
