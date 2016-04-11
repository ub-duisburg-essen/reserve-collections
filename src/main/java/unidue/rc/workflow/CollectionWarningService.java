package unidue.rc.workflow;


import org.apache.commons.configuration.ConfigurationException;

import java.time.LocalDate;

/**
 * A <code>CollectionWarningService</code> is able to send warning messages due to expiration of
 * {@link unidue.rc.model.ReserveCollection}s.
 * Created by nils on 21.03.16.
 */
public interface CollectionWarningService {

    /**
     * <p>Sends warning mail messages to users that have to be informed that their reserve collections are going to
     * expire in a certain amount of time. A message has to be send if a collections meets the following conditions:</p>
     * <ul>
     * <li>The collection is active ({@link unidue.rc.model.ReserveCollectionStatus#ACTIVE})</li>
     * <li>The collection is not marked to dissolve</li>
     * <li>A warning has not already been send</li>
     * </ul>
     * <p>The days for calculation of warning messages are configured in the properties
     * <code>days.until.first.warning</code> and <code>days.until.second.warning</code>.</p>
     *
     * @param baseDate Base date that is used for calculation. Normally {@link LocalDate#now()}
     * @throws ConfigurationException thrown on invalid configuration given for calculation
     */
    void sendWarnings(LocalDate baseDate) throws ConfigurationException;
}
