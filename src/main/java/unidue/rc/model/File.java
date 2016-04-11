package unidue.rc.model;


import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._File;

public class File extends _File implements IntPrimaryKey, EntryValue, ResourceContainer, CollectionVisitable {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("id", getId())
                .append("description", getDescription())
                .append("resource", getResource())
                .append("entry", getEntry())
                .toString();
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);

        Resource resource = getResource();
        if (resource != null)
            resource.accept(visitor);

        visitor.didVisit(this);
    }
}
