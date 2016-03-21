package unidue.rc.model.auto;

import java.util.Date;

import org.apache.cayenne.CayenneDataObject;

import unidue.rc.model.Mail;
import unidue.rc.model.ReserveCollection;

/**
 * Class _Warning was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Warning extends CayenneDataObject {

    public static final String CALCULATED_FOR_PROPERTY = "calculatedFor";
    public static final String USER_ID_PROPERTY = "userID";
    public static final String MAIL_PROPERTY = "mail";
    public static final String RESERVE_COLLECTION_PROPERTY = "reserveCollection";

    public static final String COLLECTION_ID_PK_COLUMN = "collectionID";
    public static final String MAIL_ID_PK_COLUMN = "mailID";
    public static final String USER_ID_PK_COLUMN = "userID";

    public void setCalculatedFor(Date calculatedFor) {
        writeProperty(CALCULATED_FOR_PROPERTY, calculatedFor);
    }
    public Date getCalculatedFor() {
        return (Date)readProperty(CALCULATED_FOR_PROPERTY);
    }

    public void setUserID(Integer userID) {
        writeProperty(USER_ID_PROPERTY, userID);
    }
    public Integer getUserID() {
        return (Integer)readProperty(USER_ID_PROPERTY);
    }

    public void setMail(Mail mail) {
        setToOneTarget(MAIL_PROPERTY, mail, true);
    }

    public Mail getMail() {
        return (Mail)readProperty(MAIL_PROPERTY);
    }


    public void setReserveCollection(ReserveCollection reserveCollection) {
        setToOneTarget(RESERVE_COLLECTION_PROPERTY, reserveCollection, true);
    }

    public ReserveCollection getReserveCollection() {
        return (ReserveCollection)readProperty(RESERVE_COLLECTION_PROPERTY);
    }


}