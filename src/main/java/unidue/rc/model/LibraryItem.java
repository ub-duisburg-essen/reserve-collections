package unidue.rc.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import java.util.Date;

/**
 * A <code>LibraryItem</code> is usually a special {@link Entry}, that represents a physical item inside the library.
 * Therefore it is usually safe to use it as an {@link Entry}.
 *
 * @author Nils Verheyen
 * @see Book
 * @see BookChapter
 * @see JournalArticle
 * @since 12.12.13 15:18
 */
public interface LibraryItem extends ResourceContainer {

    /**
     * Returns the id of this library item object. This is usually the entry id.
     *
     * @return id of the item
     */
    Integer getId();

    /**
     * Returns the {@link ReserveCollection} that this item belongs to.
     *
     * @return the collection the item belongs to
     */
    ReserveCollection getReserveCollection();

    /**
     * Returns the date this library item was last modified.
     *
     * @return the {@link Date} of modification
     */
    Date getModified();

    /**
     * The signature of this item. It may be possible that the signature is not present, because the item is not yet
     * present inside the library.
     *
     * @return the signature, if one is given, <code>null</code> otherwise.
     */
    String getSignature();

    /**
     * Returns the title of this item, usually the entry title.
     *
     * @return the main title of the item
     */
    String getTitle();
}
