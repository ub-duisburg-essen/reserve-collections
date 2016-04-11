package unidue.rc.workflow;


import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

/**
 * A <code>NumberGeneratorStrategy</code> can be used to generate {@linkplain ReserveCollectionNumber} objects that will
 * be used in reserve collections.
 *
 * @author Nils Verheyen
 * @see InsertNumberGenerator
 * @see AppendNumberGenerator
 */
public interface NumberGeneratorStrategy {

    ReserveCollectionNumber buildNumber(LibraryLocation location);
}
