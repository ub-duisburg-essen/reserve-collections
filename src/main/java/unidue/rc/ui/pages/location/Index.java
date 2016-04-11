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
package unidue.rc.ui.pages.location;


import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.tree.DefaultTreeModel;
import org.apache.tapestry5.tree.TreeModel;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.LibraryLocationTreeModel;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

/**
 * @author Nils Verheyen
 * @since 29.11.13 13:42
 */
@BreadCrumb(titleKey = "locations.title")
@ProtectedPage
public class Index {

    @Inject
    private LibraryLocationDAO locationDAO;

    @Property
    private LibraryLocation location;

    private final LibraryLocationTreeModel libraryLocationModel = new LibraryLocationTreeModel();

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_LOCATIONS)
    void onSetupRender() {}

    public TreeModel<LibraryLocation> getLibraryLocationModel() {
        return new DefaultTreeModel<LibraryLocation>(new LibraryLocationValueEncoder(locationDAO),
                libraryLocationModel, locationDAO.getRootLocations());
    }
}
