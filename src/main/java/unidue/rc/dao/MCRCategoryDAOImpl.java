package unidue.rc.dao;


import miless.model.MCRCategory;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;

public class MCRCategoryDAOImpl implements MCRCategoryDAO {

    @Override
    public MCRCategory getCategoryById(Integer id) {

        assert id != null;

        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(), MCRCategory.class, id);
    }

}
