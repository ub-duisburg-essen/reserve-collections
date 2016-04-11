package unidue.rc.workflow;


import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.LibraryLocation;

/**
 * A {@code LibraryLocationService} instance should be used to create or modify instances of {@link LibraryLocation}
 * objects. Do not use the {@link unidue.rc.dao.LibraryLocationDAO} to modify objects because other objects may
 * need to be modified.
 *
 * @author Nils Verheyen
 * @since 16.12.14 08:01
 */
public interface LibraryLocationService {

    /**
     * Creates target location in backend.
     *
     * @param location location to create
     * @throws CommitException thrown if any object could not be saved
     * @see unidue.rc.dao.LibraryLocationDAO#create(unidue.rc.model.LibraryLocation)
     */
    void create(LibraryLocation location) throws CommitException;

    /**
     * Updates target location in backend and calls all relevant workflow services that need to be informed that a
     * location has changed.
     *
     * @param location location to update
     * @throws CommitException thrown if any object could not be saved
     */
    void update(LibraryLocation location) throws CommitException;

    /**
     * Removes target location in backend and calls all relevant workflow services that need to be informed that a
     * location has been deleted.
     *
     * @param location location to delete
     * @throws DeleteException thrown if any object could not be deleted
     */
    void delete(LibraryLocation location) throws DeleteException;
}
