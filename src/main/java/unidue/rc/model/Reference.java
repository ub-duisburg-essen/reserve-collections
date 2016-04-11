package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;
import unidue.rc.model.auto._Reference;

public class Reference extends _Reference implements EntryValue, IntPrimaryKey, ResourceContainer, CollectionVisitable {

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);

        Resource resource = getResource();
        if (resource != null)
            resource.accept(visitor);

        visitor.didVisit(this);
    }

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    public String getYearOfPublicationAsString() {
        return getYearOfPublication() != null
                ? getYearOfPublication().toString()
                : StringUtils.EMPTY;
    }
}
