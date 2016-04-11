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
package unidue.rc.ui.pages.copyright;


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
 * Created by marcus.koesters on 12.08.15.
 */
@BreadCrumb(titleKey = "copyright.title")
public class Index {

    @Inject
    private Logger log;

    @Inject
    @Path("context:copyright/copyright.html")
    private Asset copyrightAsset;

    @Inject
    @Path("context:copyright/Merkblatt_52a_2016-02.pdf")
    @Property(write = false)
    private Asset leafletAsset;

    @Property(write = false)
    private String copyright;

    @SetupRender
    void onSetupRender() {
        try (InputStream input = copyrightAsset.getResource().openStream()) {
            copyright = IOUtils.toString(input);
        } catch (IOException e) {
            log.error("could not read copyright", e);
        }
    }
}
