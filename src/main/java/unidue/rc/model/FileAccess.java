package unidue.rc.model;


import unidue.rc.model.auto._FileAccess;

public class FileAccess extends _FileAccess implements IntPrimaryKey {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }
}
