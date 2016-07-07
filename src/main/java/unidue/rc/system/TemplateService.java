package unidue.rc.system;

import miless.model.User;
import unidue.rc.model.ReserveCollection;

import java.io.IOException;

/**
 * A <code>TemplateService</code> can be used to build large scale templates of specific types through a
 * {@linkplain Builder} interface.
 *
 * @see VelocityService
 */
public interface TemplateService {

    /**
     * Creates a new builder instance with which templates can be build.
     *
     * @return the created builder instance
     */
    Builder builder();

    /**
     * Builds a string for the authors of a reserve collection.
     *
     * @param collection collection to use
     * @param divider    divider which is used to divide multiple authors
     * @return result string
     */
    String buildAuthors(ReserveCollection collection, String divider);

    /**
     * Builds a string for the origins of a reserve collection. The origin highest inside origin tree is on the first
     * position.
     *
     * @param collection collection to use
     * @param divider    divider which is used to divide multiple origins
     * @return result string
     */
    String buildOrigin(ReserveCollection collection, String divider);

    /**
     * Builds a string for the origins of a user. The origin highest inside origin tree is on the first
     * position.
     *
     * @param user    user to use
     * @param divider divider which is used to divide multiple origins
     * @return result string
     */
    String buildOrigin(User user, String divider);

    /**
     * Defines the builder which is specific for each template service.
     */
    interface Builder {

        /**
         * Builds the result of a template
         *
         * @param template path to the template that should be used
         * @return the build template
         * @throws IOException thrown if the template could not be build cause of reading the template or writing the
         *                     output
         */
        String build(String template) throws IOException;

        /**
         * Puts a parameter that is used inside the template.
         *
         * @param key key used inside the object
         * @param o   object that is finally shown
         * @return this builder instance
         */
        Builder put(String key, Object o);
    }
}
