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

/**
 * An instance of <code>IntPrimaryKey</code> is a simple pojo from this object model which uses a integer as primary
 * key.
 *
 * @author Nils Verheyen
 * @since 13.03.14 15:55
 */
public interface IntPrimaryKey {

    /**
     * Returns the Integer primary key of this object
     *
     * @return the id of the object
     */
    Integer getId();
}
