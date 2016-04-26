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
package unidue.rc.ui.services;


import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import unidue.rc.ui.components.Layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The <code>GlobalJavaScriptStack</code> provides all javascript files, which
 * should be available across the entier application.
 *
 * @author Nils Verheyen
 * @see AppModule
 * @see Layout
 */
public class GlobalJavaScriptStack implements JavaScriptStack {

    private final AssetSource assetSource;

    public GlobalJavaScriptStack(AssetSource assetSource) {
        this.assetSource = assetSource;
    }

    @Override
    public List<String> getStacks() {
        return Collections.emptyList();
    }

    @Override
    public List<Asset> getJavaScriptLibraries() {

        return Stream.of(
                assetSource.getContextAsset("vendor/jquery-1.11.2.min.js", null),
                assetSource.getContextAsset("vendor/jquery-ui/js/jquery-ui-1.10.3.custom.min.js", null),
                assetSource.getContextAsset("vendor/modernizr-2.6.2-respond-1.1.0.min.js", null),
                assetSource.getContextAsset("vendor/bootstrap/js/bootstrap.js", null),
                assetSource.getContextAsset("vendor/toastr/toastr.min.js", null))
                .collect(Collectors.toList());
    }

    @Override
    public List<StylesheetLink> getStylesheets() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getInitialization() {
        return null;
    }

}
