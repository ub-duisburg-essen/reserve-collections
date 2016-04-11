package unidue.rc.ui.selectmodel;


import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>LibraryLocationListSelectModel</code> can be used as a {@link org.apache.tapestry5.SelectModel} that simply
 * returns {@link LibraryLocation} objects as {@link OptionModel}s.
 *
 * @author Nils Verheyen
 * @since 02.12.13 09:21
 */
public class LibraryLocationListSelectModel extends LibraryLocationSelectModel {

    private final LibraryLocationDAO dao;

    public LibraryLocationListSelectModel(LibraryLocationDAO locationDAO) {
        super(locationDAO);
        this.dao = locationDAO;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }

    @Override
    public List<OptionModel> getOptions() {
        List<LibraryLocation> locations = dao.getLocations();
        List<OptionModel> result = new ArrayList<>();
        for (LibraryLocation location : locations) {
            result.add(new OptionModelImpl(location.getName(), location));
        }
        return result;
    }
}
