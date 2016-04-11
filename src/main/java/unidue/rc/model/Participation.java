package unidue.rc.model;


import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._Participation;

public class Participation extends _Participation implements IntPrimaryKey, CollectionVisitable {

    @Override
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
               ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
               : null;
    }

    /**
     * Do NOT use this method to set the reserve collection id inside this participation. Use
     * {@link #setReserveCollection(ReserveCollection)} instead.
     *
     * @param collectionID see desc.
     * @throws UnsupportedOperationException everytime
     */
    @Override
    public void setCollectionID(Integer collectionID) {
        throw new UnsupportedOperationException("set of collection id is not allowed, use setReserveCollection instead");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("userId", getUserId())
                .append("role", getRole())
                .append("collection", getReserveCollection())
                .toString();
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
