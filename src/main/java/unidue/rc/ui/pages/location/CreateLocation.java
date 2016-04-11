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


import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

/**
 * @author Nils Verheyen
 * @since 29.11.13 14:18
 */
@BreadCrumb(titleKey = "new.location")
@ProtectedPage
public class CreateLocation {

    @Property
    @Validate("required")
    private String locationName;

    @Property
    private Boolean physical;

    @Property
    private LibraryLocation parentLocation;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Component(id = "location_form")
    private Form form;

    @Inject
    private Messages messages;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_LOCATIONS)
    void onSetupRender() {}

    @OnEvent(value = EventConstants.SUCCESS, component = "location_form")
    Object onSuccess() {

        LibraryLocation location = new LibraryLocation();
        location.setName(locationName);
        location.setPhysical(physical);
        location.setParentLocation(parentLocation);
        try {
            locationDAO.create(location);
            return Index.class;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.location", location));
            return null;
        }
    }

    public SelectModel getLocationSelectModel() {
        return new LibraryLocationListSelectModel(locationDAO);
    }

    public ValueEncoder<LibraryLocation> getLocationEncoder() {
        return new LibraryLocationValueEncoder(locationDAO);
    }
}
