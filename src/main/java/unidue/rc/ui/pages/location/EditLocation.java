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
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.LibraryLocationService;

/**
 * @author Nils Verheyen
 * @since 02.12.13 10:30
 */
@BreadCrumb(titleKey = "edit")
@ProtectedPage
public class EditLocation {

    @Inject
    private Logger log;

    @Inject
    private LibraryLocationService locationService;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Inject
    private Messages messages;

    @Property
    private LibraryLocation location;

    @Component(id = "location_form")
    private Form form;

    @Component(id = "locationName")
    private TextField locationNameField;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_LOCATIONS)
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("edit.location", location.getName()));
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer locationId) {
        this.location = locationDAO.getLocationById(locationId);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return location.getId();
    }

    @OnEvent(value = EventConstants.ACTION, component = "deleteLocation")
    Object onDeleteLocation() {
        try {
            locationService.delete(location);
            return Index.class;
        } catch (DeleteException e) {
            form.recordError(messages.format("error.msg.could.not.delete.location", location.getName()));
            return null;
        }
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "location_form")
    void onValidate() {
        if (location.getName() == null || location.getName().isEmpty())
            form.recordError(locationNameField, messages.get("locationName-required-message"));
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "location_form")
    Object onSuccess() {

        try {
            locationService.update(location);
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
