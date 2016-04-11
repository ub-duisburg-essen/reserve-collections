package unidue.rc.io;


import unidue.rc.model.ReserveCollection;
import unidue.rc.model.rss.Channel;

/**
 * Created by nils on 20.10.15.
 */
public interface CollectionRSSWriter {

    /**
     * Creates a rss channel of given collection with its entries as items.
     *
     * @param collection contains the collection that the stream should be generated for
     * @return the channel that can be written to the client
     */
    Channel serialize(ReserveCollection collection);
}
