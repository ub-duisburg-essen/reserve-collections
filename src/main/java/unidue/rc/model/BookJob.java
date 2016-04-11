package unidue.rc.model;


import unidue.rc.model.auto._BookJob;

public class BookJob extends _BookJob implements IntPrimaryKey {

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }
}
