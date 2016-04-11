package unidue.rc.model;


import unidue.rc.model.auto._Html;

public class Html extends _Html implements EntryValue, IntPrimaryKey, CollectionVisitable {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
