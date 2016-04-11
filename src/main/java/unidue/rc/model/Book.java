package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;
import unidue.rc.model.auto._Book;

import java.util.Date;

public class Book extends _Book implements LibraryItem, EntryValue, IntPrimaryKey, CollectionVisitable {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }


    @Override
    public ReserveCollection getReserveCollection() {
        return getEntry().getReserveCollection();
    }

    @Override
    public Date getModified() {
        return getEntry().getModified();
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);

        Resource resource = getResource();
        if (resource != null)
            resource.accept(visitor);

        visitor.didVisit(this);
    }

    public String getYearOfPublicationAsString() {
        return getYearOfPublication() != null
                ? getYearOfPublication().toString()
                : StringUtils.EMPTY;
    }
}
