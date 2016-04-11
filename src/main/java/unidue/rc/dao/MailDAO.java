package unidue.rc.dao;


import unidue.rc.model.Mail;

import java.util.List;

/**
 * Created by nils on 07.07.15.
 */
public interface MailDAO extends BaseDAO {

    /**
     * Returns all mails that could not be send.
     *
     * @return see description
     */
    List<Mail> getUnsendMails();
}
