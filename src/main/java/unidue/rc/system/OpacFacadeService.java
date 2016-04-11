package unidue.rc.system;


import unidue.rc.model.OpacFacadeBook;
import unidue.rc.model.OpacFacadeFind;
import unidue.rc.model.OpacFacadeLibraryData;

/**
 * With an <code>OpacFacadeService</code> one is able to execute services on the catalogue behind the opac-facade
 * project.
 *
 * @author Nils Verheyen
 * @see <a href="http://redmine.ub.uni-due.de/projects/opac-facade/">OPAC Facade in Redmine</a>
 * @see <a href="https://server.mycore.de/svn/opac-facade/trunk/">SVN Repo</a>
 * @since 16.09.13 15:44
 */
public interface OpacFacadeService {

    /**
     * Executes the <code>find</code> operation on the facade
     *
     * @param searchString the string to search for
     * @return an {@linkplain OpacFacadeFind} object if a result could be loaded, <code>null</code> otherwise
     */
    OpacFacadeFind search(String searchString);

    /**
     * Executes the <code>docDetails</code> operation on the facade
     *
     * @param docNumber opacs doc number of the {@linkplain OpacFacadeBook}
     * @return an {@linkplain OpacFacadeBook} if one could be found, <code>null</code> otherwise
     */
    OpacFacadeBook getDetails(String docNumber);

    /**
     * Returns the library data associated with the record with target doc number.
     *
     * @param docNumber opacs doc number of the {@linkplain OpacFacadeLibraryData}
     * @return an {@linkplain OpacFacadeLibraryData} object if one could be found by target number, <code>null</code>
     *         otherwise
     */
    OpacFacadeLibraryData getLibraryData(String docNumber);
}
