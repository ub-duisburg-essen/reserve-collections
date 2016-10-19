package unidue.rc.model;

import unidue.rc.model.auto._OrderMailRecipient;

public class OrderMailRecipient extends _OrderMailRecipient implements IntPrimaryKey {

    @Override
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
               ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
               : null;
    }
}
