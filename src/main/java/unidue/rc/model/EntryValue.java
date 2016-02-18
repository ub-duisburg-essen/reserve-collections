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

import org.apache.cayenne.Persistent;

/**
 * An <code>EntryValue</code> is an object that can be assigned to a {@link unidue.rc.model.ReserveCollection} via an
 * {@link unidue.rc.model.Entry}
 *
 * @author Nils Verheyen
 * @since 18.03.14 11:18
 */
public interface EntryValue extends Persistent {

    /**
     * Returns the entry assigned to this value.
     *
     * @return the entry assigned to this value.
     */
    Entry getEntry();

    /**
     * Sets the entry to this value.
     *
     * @param entry sets the entry of this value
     */
    void setEntry(Entry entry);
}
