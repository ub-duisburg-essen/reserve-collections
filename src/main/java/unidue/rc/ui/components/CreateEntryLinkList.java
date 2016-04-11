/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.components;


import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import unidue.rc.model.ReserveCollection;

/**
 * <p> A <code>CreateEntryLinkList</code> can be used as a component to render a bootstrap navbar list with links to
 * create new entry for a new reserve collection. </p>
 * <pre>
 *     {@code <t:createentrylinklist collection="..."/>}
 * </pre>
 *
 * @author Nils Verheyen
 * @since 11.09.13 09:35
 */
public class CreateEntryLinkList {

    @Parameter(required = true, allowNull = false, name = "collection")
    @Property
    private ReserveCollection collection;
}
