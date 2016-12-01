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
package unidue.rc.ui.pages.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;

/**
 * Created by nils on 28.11.16.
 */
@Import(library = {"context:js/delete.scannable.files.js"})
public class DeleteScannableFiles {

    @Inject
    private Logger log;

    @Inject
    private ScannableService scannableService;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @InjectComponent("delete_all_files_form")
    private Form form;

    @InjectComponent("formZone")
    private Zone formZone;

    @InjectComponent("progressZone")
    private Zone progressZone;

    @Property
    private String authorizationCode;

    @Property
    private String authorizationCodeConfirmation;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String progress;


    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "delete_all_files_form")
    Object onValidateForm() {

        if (StringUtils.isAnyBlank(authorizationCode, authorizationCodeConfirmation)
                || !StringUtils.equals(authorizationCode, authorizationCodeConfirmation)) {

            form.recordError(messages.get("error.msg.auth.codes.does.not.match"));
            return this;
        }

        try {
            scannableService.deleteAllFiles(authorizationCode, this::onUpdateProcess);
        } catch (CommitException e) {
            log.error("could not delete all files", e);
            form.recordError(messages.get("error.msg.could.update.resources.check.log"));
        } catch (IllegalArgumentException e) {
            log.warn("invalid authorization code", e);
            form.recordError(messages.get("error.msg.invalid.auth.code"));
        } catch (IOException e) {
            log.error("could not create log", e);
            form.recordError(messages.get("error.msg.could.not.write.log"));
        }
        return this;
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "delete_all_files_form")
    void onSuccess() {
    }

    private void onUpdateProcess(Integer current, Integer total) {
        log.info("deleted " + current + " of " + total);
        progress = current == 0
                   ? StringUtils.EMPTY
                   : Integer.toString((int) ((100.0 * current) / total));
    }
}