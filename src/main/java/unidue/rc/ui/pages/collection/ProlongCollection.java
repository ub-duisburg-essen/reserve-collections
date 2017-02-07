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
package unidue.rc.ui.pages.collection;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Select;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.model.ReserveCollection;
import unidue.rc.ui.selectmodel.ExpirySelectModel;
import unidue.rc.ui.valueencoder.TimeValueEncoder;
import unidue.rc.workflow.CollectionService;

import java.util.Calendar;

/**
 * Created by nils on 14.12.15.
 */
@BreadCrumb(titleKey = "prolong")
@Import(library = {"context:js/prolong.collection.js"})
public class ProlongCollection {

    private static final String PROLONG_DATE_FORMAT = "dd.MM.yyyy";

    private static final int DEFAULT_DISSOLVE_VALUE = 4;

    private static final int DISSOLVE_MIN_VALUE = 1;

    private static final int DISSOLVE_MAX_VALUE = 8;

    @Inject
    private Logger log;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    private CollectionService collectionService;

    @Inject
    private Messages messages;

    @Component(id = "prolong_form")
    private Form prolongForm;

    @Component(id = "expiry")
    private Select expirationField;

    @Component(id = "expected_participations")
    private TextField expectedParticipationsField;

    @Component(id = "dissolve_form")
    private Form dissolveForm;

    @Component(id = "dissolve_in_weeks")
    private TextField dissolveInWeeksField;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String successMessage;

    @Property
    private int collectionID;

    @Property
    private String prolongCode;

    @Property
    private Integer expectedParticipations;

    @Property
    private ReserveCollection collection;

    @Property
    @Validate("required")
    private Calendar expiry;

    @Property
    @Validate("required,max=" + DISSOLVE_MAX_VALUE + ",min=" + DISSOLVE_MIN_VALUE)
    private int dissolveInWeeks;

    @SetupRender
    void onSetupRender() {
        this.dissolveInWeeks = DEFAULT_DISSOLVE_VALUE;
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(int collectionID, String prolongCode) {

        this.collectionID = collectionID;
        this.prolongCode = prolongCode;

        this.collection = baseDAO.get(ReserveCollection.class, collectionID);
        this.expectedParticipations = collection != null
                                      ? collection.getExpectedParticipations()
                                      : null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object[] onPassivate() {
        return new Object[]{collectionID, prolongCode};
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "prolong_form")
    void onValidateProlong() {

        if (expectedParticipations == null) {
            prolongForm.recordError(expectedParticipationsField, messages.format("error.msg.expected.participations.required"));
            return;
        }

        try {
            collection.setExpectedParticipations(expectedParticipations);
            collectionService.prolong(collection, prolongCode, expiry.getTime());
        } catch (ContextedException e) {
            log.warn("could not prolong collection " + collection + " with code " + prolongCode, e);
            prolongForm.recordError(expirationField, messages.format("error.msg.could.not.prolong.collection"));
        } catch (CommitException e) {
            log.error("could not save collection " + collection, e);
            prolongForm.recordError(expirationField, messages.format("error.msg.could.not.commit.collection", collection.getTitle()));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "prolong_form")
    void onSuccessOfProlong() {
        successMessage = getProlongedInfoMessage();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "dissolve_form")
    void onValidateDissolve() {

        if (dissolveInWeeks > DISSOLVE_MAX_VALUE) {
            dissolveForm.recordError(dissolveInWeeksField, messages.format("dissolve_in_weeks-max-message", DISSOLVE_MAX_VALUE));
            return;
        } else if (dissolveInWeeks < DISSOLVE_MIN_VALUE) {
            return;
        }

        DateTime validTo = new DateTime(collection.getValidTo());
        DateTime dissolveDate = validTo.plusWeeks(dissolveInWeeks);
        try {
            collection.setDissolveAt(dissolveDate.toDate());
            collectionService.update(collection);
        } catch (CommitException e) {
            dissolveForm.recordError(dissolveInWeeksField, messages.get("error.msg.could.not.dissolve.collection"));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "dissolve_form")
    void onSuccessOfDissolve() {
        successMessage = messages.format("info.msg.collection.set.to.dissolve",
                collection.getTitle(),
                DateFormatUtils.format(collection.getDissolveAt(), PROLONG_DATE_FORMAT));
    }

    public String getValidTo() {
        return DateFormatUtils.ISO_DATE_FORMAT.format(collection.getValidTo());
    }

    public void setValidTo(String value) {
    }

    public String getVisibleValidTo() {
        return DateFormatUtils.format(collection.getValidTo(), PROLONG_DATE_FORMAT);
    }

    public void setVisibleValidTo(String value) {
    }

    public boolean getShowForms() {
        return collection.getDissolveAt() == null
                && collection.getProlongUsed() == null
                && StringUtils.isBlank(successMessage);
    }

    public String getProlongedInfoMessage() {
        return messages.format("info.msg.collection.prolonged",
                collection.getTitle(),
                DateFormatUtils.format(collection.getValidTo(), PROLONG_DATE_FORMAT));
    }

    public String getDissolvedInfoMessage() {
        return messages.format("info.msg.collection.set.to.dissolve",
                collection.getTitle(),
                DateFormatUtils.format(collection.getValidTo(), PROLONG_DATE_FORMAT));
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to set the expiry for the new reserve
     * collection.
     *
     * @return the expiration select model
     */
    public SelectModel getExpirySelectModel() {
        try {
            return new ExpirySelectModel(collectionService.getCollectionProlongDates(), messages);
        } catch (ConfigurationException e) {
            log.error("configuration error", e);
            return null;
        }
    }

    public ValueEncoder<Calendar> getTimeEncoder() {
        return new TimeValueEncoder();
    }
}
