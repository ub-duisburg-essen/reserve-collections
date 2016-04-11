package unidue.rc.ui.selectmodel;


import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.SelectModelVisitor;
import org.apache.tapestry5.internal.OptionGroupModelImpl;
import org.apache.tapestry5.internal.OptionModelImpl;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> A <code>LibraryLocationSelectModel</code> can be used inside tapestrys select element as a <code>model</code> to
 * select {@link LibraryLocation} objects. </p>
 *
 * @author Nils Verheyen
 * @see LibraryLocationSelectModel#getOptions()
 * @see LibraryLocationSelectModel#getOptionGroups()
 */
public class LibraryLocationSelectModel implements SelectModel {

    /**
     * Contains the root {@link LibraryLocation} objects with no parent in DB.
     */
    private List<LibraryLocation> rootLocations;

    /**
     * DAO to load {@link LibraryLocation} objects.
     */
    private LibraryLocationDAO libraryLocationDao;

    public LibraryLocationSelectModel(LibraryLocationDAO libraryLocationDao) {
        this.libraryLocationDao = libraryLocationDao;
        initRootLibraryLocations();
    }

    /**
     * Loads the root {@link LibraryLocation} object from backend.
     */
    private void initRootLibraryLocations() {
        this.rootLocations = libraryLocationDao.getRootLocations();
    }

    /**
     * Returns all {@link LibraryLocation} objects which are root locations and contain children so they are placed as
     * label inside a <code>optgroup</code> and are not selectable.
     */
    @Override
    public List<OptionGroupModel> getOptionGroups() {
        List<OptionGroupModel> result = new ArrayList<>();
        for (LibraryLocation location : rootLocations) {
            if (location.hasChildren()) {
                result.add(new OptionGroupModelImpl(location.getName(), false, getChildLocationOptions(location)));
            }
        }
        return result;
    }

    /**
     * Returns all children of target location as a list op {@link OptionModel}s
     *
     * @param rootLocation the location of which children should be returned
     * @return a list of {@link OptionModel} objects that have target location as parent
     */
    private List<OptionModel> getChildLocationOptions(LibraryLocation rootLocation) {

        List<OptionModel> result = new ArrayList<>();
        for (LibraryLocation location : rootLocation.getChildLocations()) {
            result.add(new OptionModelImpl(location.getName(), location));
        }
        return result;
    }

    /**
     * Returns all root {@link LibraryLocation} objects, which have no children as {@link OptionModel}s.
     */
    @Override
    public List<OptionModel> getOptions() {
        List<OptionModel> result = new ArrayList<>();
        for (LibraryLocation location : rootLocations) {
            if (!location.hasChildren()) {
                result.add(new OptionModelImpl(location.getName(), location));
            }
        }
        return result;
    }

    @Override
    public final void visit(SelectModelVisitor visitor) {
        visitOptions(getOptions(), visitor);
        List<OptionGroupModel> groups = getOptionGroups();

        if (groups != null) {
            for (OptionGroupModel groupModel : groups) {
                visitor.beginOptionGroup(groupModel);

                visitOptions(groupModel.getOptions(), visitor);

                visitor.endOptionGroup(groupModel);
            }
        }

    }

    private void visitOptions(List<OptionModel> options, SelectModelVisitor vistor) {
        if (options != null) {
            for (OptionModel optionModel : options)
                vistor.option(optionModel);
        }
    }
}
