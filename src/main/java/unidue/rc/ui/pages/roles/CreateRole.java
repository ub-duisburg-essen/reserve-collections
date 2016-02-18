package unidue.rc.ui.pages.roles;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Role;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;

@BreadCrumb(titleKey = "new.role")
@ProtectedPage
public class CreateRole {

    @Property
    private Role role;

    @Property
    private String roleName;

    @Inject
    private RoleDAO dao;

    @Component(id = "create_role_form")
    private Form form;

    @Component(id = "roleName")
    private TextField roleNameField;

    @Inject
    private Messages messages;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_ROLES)
    void onSetupRender() {}

    @OnEvent(value = EventConstants.VALIDATE, component = "create_role_form")
    void onValidateRoleForm() {
        if (StringUtils.isEmpty(roleName))
            form.recordError(roleNameField, messages.get("roleName-required-message"));
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onRoleSubmitted() {

        role = new Role();
        role.setName(roleName);
        role.setIsDefault(false);

        try {
            dao.create(role);
            return Index.class;
        } catch (CommitException e) {
            form.recordError(messages.format("error.message.could.not.create.role", roleName));
            return this;
        }
    }
}
