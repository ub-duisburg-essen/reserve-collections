package unidue.rc.ui.pages.configuration;

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


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.SettingDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Setting;
import unidue.rc.model.SettingType;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.FieldCopy;
import unidue.rc.ui.ProtectedPage;

import java.util.*;
import java.util.stream.Collectors;

@Import(library = {
        "context:js/view.collection.js"
})
@BreadCrumb(titleKey = "page.config.title")
@ProtectedPage
public class Index implements SecurityContextPage {

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SettingDAO settingDAO;

    @Inject
    private Logger log;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private Locale locale;

    @Inject
    private Messages messages;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @SessionState
    private BreadCrumbList breadCrumbList;

    private Setting setting;

    @Property
    private SettingType settingType;

    // Work fields

    private boolean inFormSubmission;

    private List<Setting> settingsSubmitted;

    private int rowNum;
    private Map<Integer, FieldCopy> firstNameCopyByRowNum;

    @InjectComponent("settingsForm")
    private Form form;

    @InjectComponent("setting")
    private TextField settingField;

    @SetupRender
    public void setupRender() {

        breadCrumbList.getLastCrumb().setTitle(messages.get("page.config.title"));
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {
        inFormSubmission = false;
    }


    @OnEvent(EventConstants.PREPARE_FOR_SUBMIT)
    void onPrepareForSubmit() {
        log.debug("preparing for submit");
        inFormSubmission = true;
        settingsSubmitted = new ArrayList<>();

        // Prepare to take a copy of each editable field.
        rowNum = 0;
        firstNameCopyByRowNum = new HashMap<>();
    }

    void onValidateFromSetting() {
        rowNum++;
        firstNameCopyByRowNum.put(rowNum, new FieldCopy(settingField));
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onConfigSubmitted() {
        try {
            for (Setting setting : settingsSubmitted) {
                settingDAO.update(setting);
            }
            return this;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.setting", setting.getKey()));
            return null;
        }
    }

    @OnEvent(EventConstants.FAILURE)
    void onFailure() {
        log.debug("failed to update settings");
    }

    public List<Setting> getSettings() {
        return settingDAO.getAllSettings()
                .stream()
                .filter(setting -> setting.getType().equals(settingType))
                .sorted()
                .collect(Collectors.toList());
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;

        if (inFormSubmission) {
            log.debug("adding submitted setting " + setting.getKey());
            settingsSubmitted.add(setting);
        }
    }

    public List<SettingType> getSettingTypes() {
        return Arrays.asList(SettingType.values())
                .stream()
                .filter(type -> {
                    switch (type) {
                        case SYSTEM:
                            return securityService.isPermitted(ActionDefinition.EDIT_SETTINGS);
                        case COLLECTION:
                            return securityService.isPermitted(ActionDefinition.EDIT_COLLECTION_SETTINGS);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public String getSettingTypeLabel() {
        return messages.get(settingType.name());
    }

    public String getCompiledResult(Setting setting) {
        return config.getString(setting.getKey());
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        if (!securityService.isPermitted(ActionDefinition.EDIT_SETTINGS)
                && !securityService.isPermitted(ActionDefinition.EDIT_COLLECTION_SETTINGS))
            throw new AuthorizationException("edit of settings is not permitted");
    }
}
