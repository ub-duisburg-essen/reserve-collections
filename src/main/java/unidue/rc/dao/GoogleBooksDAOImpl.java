package unidue.rc.dao;


import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import org.apache.cayenne.di.Inject;
import org.apache.log4j.Logger;
import unidue.rc.system.SystemConfigurationService;

import java.io.IOException;

/**
 * Created by nils on 10.07.15.
 */
public class GoogleBooksDAOImpl implements GoogleBooksDAO {

    private static final String GOOGLE_APPLICATION_NAME = "Online Semesterapparat/1.0";

    private static final Logger LOG = Logger.getLogger(GoogleBooksDAO.class.getName());

    /**
     * Builded {@link Books} client, that should be used inside to retrieve information of books api.
     */
    private final Books BOOKS;

    private SystemConfigurationService config;

    public GoogleBooksDAOImpl(@Inject SystemConfigurationService config) {
        this.config = config;

        BOOKS = new Books.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                .setGoogleClientRequestInitializer(new BooksRequestInitializer(config.getString("google.books.api.key")))
                .setApplicationName(GOOGLE_APPLICATION_NAME)
                .build();
    }

    public String getThumbnail(String isbn) {

        // normalize isbn so that it only contains numbers
        isbn = isbn.replaceAll("[^0-9]", "");

        String query = "isbn:" + isbn;
        try {
            long time = System.currentTimeMillis();

            Books.Volumes.List volumesList = BOOKS.volumes().list(query);
            Volumes volumes = volumesList.execute();
            int totalItems = volumes.getTotalItems() == null ? 0 : volumes.getTotalItems();
            if (totalItems == 1 && volumes.getItems() != null) {

                Volume volume = volumes.getItems().get(0);
                Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
                if (volumeInfo != null) {

                    Volume.VolumeInfo.ImageLinks imageLinks = volumeInfo.getImageLinks();
                    if (imageLinks != null) {

                        String thumbnail = imageLinks.getThumbnail();
                        // LOG.debug("thumbnail " + thumbnail + " for isbn " +
                        // isbn);
                        LOG.debug("thumbnail retrieval time -> " + (System.currentTimeMillis() - time) + " ms");
                        return thumbnail;
                    }
                }
            }

        } catch (IOException e) {
            LOG.error("couldn't execute thumbnail request", e);
        }
        return null;
    }
}
