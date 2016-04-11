package unidue.rc.model;


import unidue.rc.model.auto._Permission;

public class Permission extends _Permission implements IntPrimaryKey{

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (getInstanceID() != null ? !getInstanceID().equals(that.getInstanceID()) : that.getInstanceID() != null) return false;
        if (!getUserID().equals(that.getUserID())) return false;
        return getAction().equals(that.getAction());

    }

    @Override
    public int hashCode() {
        int result = getInstanceID() != null ? getInstanceID().hashCode() : 0;
        result = 31 * result + getUserID().hashCode();
        result = 31 * result + getAction().hashCode();
        return result;
    }
}
