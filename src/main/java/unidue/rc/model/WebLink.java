package unidue.rc.model;


import unidue.rc.model.auto._WebLink;

public class WebLink extends _WebLink implements IntPrimaryKey, EntryValue, CollectionVisitable {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, Entry.ID_PK_COLUMN);
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
