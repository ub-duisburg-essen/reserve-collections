package unidue.rc.model;


import org.apache.cayenne.ExtendedEnumeration;

/**
 * Cayenne is not very usable with abstraction, therefore this interface represents a status of a job. Multiple jobs can
 * be used together with this status. A status is normally just a enumeration, therefore the methods declared in this
 * interface match them.
 *
 * @author Nils Verheyen
 * @see BookJobStatus
 * @see ScanJobStatus
 * @see unidue.rc.model.BookJob
 * @see unidue.rc.model.ScanJob
 * @since 12.12.13 12:45
 */
public interface JobStatus extends ExtendedEnumeration {

    /**
     * Returns the name of this status.
     *
     * @return see description
     */
    String name();

    /**
     * Returns the int value of this status
     *
     * @return see description
     */
    Integer getValue();

    /**
     * Compares this status to another. The result is similar to the result of a enum.
     *
     * @param other other status to compare to
     * @return see description
     */
    int compareTo(JobStatus other);
}
