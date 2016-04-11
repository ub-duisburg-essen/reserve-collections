package unidue.rc.model;


import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.ObjectId;

/**
 * @author Nils Verheyen
 * @since 12.03.14 10:04
 */
public class CayenneUtils {

    public static Integer getID(CayenneDataObject object, String idPkColumn) {

        ObjectId objectId = object.getObjectId();
        return (objectId != null && !objectId.isTemporary())
                ? (Integer) objectId.getIdSnapshot().get(idPkColumn)
                : null;
    }
}
