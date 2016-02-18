package unidue.rc.ui.pages;

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

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nils on 31.08.15.
 */
@BreadCrumb(titleKey = "faq.title")
public class FAQ {

    @Inject
    private Logger log;

    @Inject
    @Path("context:faq/faq.html")
    private Asset faqAsset;

    @Property
    private String faq;

    @SetupRender
    void onSetupRender() {
        try (InputStream input = faqAsset.getResource().openStream()) {
            faq = IOUtils.toString(input);
        } catch (IOException e) {
            log.error("could not read faq", e);
        }
    }
}
