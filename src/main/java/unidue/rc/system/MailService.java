package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.User;
import org.apache.commons.mail.EmailException;
import unidue.rc.model.Entry;
import unidue.rc.model.Mail;
import unidue.rc.model.ReserveCollection;

/**
 * Created by nils on 24.06.15.
 */
public interface MailService {

    /**
     * Tries to send target {@link Mail}. If the mail is valid, but could not be send
     * it is saved in backend for later use.
     *
     * @param mail mail that should be send
     * @throws EmailException thrown if mail send was unsuccessful
     */
    void sendMail(Mail mail) throws EmailException;

    /**
     * Builds a string of the form {@code <location name> - <collection number> - <entry id>: <context> <[authors]>}
     *
     * @param entry   see description
     * @param context see description
     * @param authors see description
     * @return the build subject
     */
    String buildSubject(Entry entry, String context, String authors);

    /**
     * Builds a string of the form {@code <location 1> &raquo; <location 2> &raquo; <location 3>} targeting
     * the location of given collection.
     *
     * @param collection see description
     * @return the build subject
     */
    String buildOrigin(ReserveCollection collection);

    /**
     * Builds a string of the form {@code <location 1> &raquo; <location 2> &raquo; <location 3>} targeting
     * the location of given user.
     *
     * @param user see description
     * @return the build origin string
     */
    String buildOrigin(User user);

    /**
     * Builds a string of the form {@code <author>, <author>, <author>} targeting
     * the location of given collection.
     *
     * @param collection see description
     * @return the build author string
     */
    String buildAuthors(ReserveCollection collection);

    /**
     * Creates a url that points to given collection.
     *
     * @param collection see description
     * @return url that points to target collection
     */
    String createCollectionLink(ReserveCollection collection);

    /**
     * Creates a url that points to given entry inside a collection.
     *
     * @param entry see description
     * @return url that points to target entry
     */
    String createEntryLink(Entry entry);

    /**
     * Creates a new {@link unidue.rc.system.MailServiceImpl.MailBuilder} with which a mail can be created.
     * A {@link Mail} has the following requirements:
     * <ul>
     * <li>from</li>
     * <li>one or more recipients</li>
     * <li>subject</li>
     * <li>context</li>
     * </ul>
     *
     * @param templateName name of the html template that should be used
     * @return the new mail builder
     */
    MailServiceImpl.MailBuilder builder(String templateName);
}
