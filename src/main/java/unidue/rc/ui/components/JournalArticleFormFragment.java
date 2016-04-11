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


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.Headline;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ReserveCollection;
import unidue.rc.workflow.ScannableService;

import java.util.List;

/**
 * Created by nils on 31.07.15.
 */
public class JournalArticleFormFragment {

    @Inject
    private Logger log;

    @Parameter(required = true, allowNull = false)
    @Property
    private JournalArticle article;

    @Parameter(required = true, allowNull = false)
    @Property
    private ReserveCollection collection;

    @Parameter(required = false, allowNull = false)
    @Property
    private boolean isVisible = true;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private Messages messages;

    // screen fields

    @Property(read = false)
    @Persist
    private List<UploadedFile> uploads;

    @Parameter
    private String url;

    @Property(read = false)
    private Headline headline;

    // working field

    @Component(id = "articletitle")
    private TextField articleTitleField;

    @Component(id = "journaltitle")
    private TextField journalTitleField;

    @Component(id = "journalpagesfrom")
    private TextField pageStartField;

    @Component(id = "journalpagesto")
    private TextField pageEndField;

    @Component(id = "journalvolume")
    private TextField journalVolumeField;


    public List<UploadedFile> getUploads() {
        return uploads;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Headline getHeadline() {
        return headline;
    }

    public void validate(Form form) {
        if (StringUtils.isEmpty(article.getArticleTitle()))
            form.recordError(articleTitleField, messages.get("articleTitle-required-message"));

        if (StringUtils.isEmpty(article.getJournalTitle()))
            form.recordError(journalTitleField, messages.get("journalTitle-required-message"));

        if (StringUtils.isEmpty(article.getPageStart()))
            form.recordError(pageStartField, messages.get("journalPagesFrom-required-message"));

        if (StringUtils.isEmpty(article.getPageEnd()))
            form.recordError(pageEndField, messages.get("journalPagesTo-required-message"));
    }
}
