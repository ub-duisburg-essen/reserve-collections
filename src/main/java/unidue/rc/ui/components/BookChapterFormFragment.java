package unidue.rc.ui.components;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.model.BookChapter;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;

/**
 * Created by nils on 31.07.15.
 */
public class BookChapterFormFragment {

    @Parameter
    private Form form;

    @Parameter
    @Property
    private BookChapter chapter;

    @Parameter
    @Property
    private ReserveCollection collection;

    @Property(read = false)
    @Parameter
    private Headline headline;

    @Parameter
    @Property
    private boolean isVisible;

    @Inject
    private Messages messages;

    @Component(id = "chaptertitle")
    private TextField chapterTitleField;

    @Component(id = "booktitle")
    private TextField bookTitleField;

    @Component(id = "bookpagesfrom")
    private TextField bookPageStartField;

    @Component(id = "bookpagesto")
    private TextField bookPageEndField;

    public void validate() {
        if (StringUtils.isEmpty(chapter.getBookTitle()))
            form.recordError(bookTitleField, messages.get("booktitle-required-message"));

        if (StringUtils.isEmpty(chapter.getChapterTitle()))
            form.recordError(chapterTitleField, messages.get("chaptertitle-required-message"));

        if (StringUtils.isEmpty(chapter.getPageStart()))
            form.recordError(bookPageStartField, messages.get("bookPagesFrom-required-message"));

        if (StringUtils.isEmpty(chapter.getPageEnd()))
            form.recordError(bookPageEndField, messages.get("bookPagesTo-required-message"));
    }

    public Headline getHeadline() {
        return headline;
    }
}
