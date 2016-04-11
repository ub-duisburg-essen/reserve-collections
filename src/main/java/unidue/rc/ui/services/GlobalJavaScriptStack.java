package unidue.rc.ui.services;


import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import unidue.rc.ui.components.Layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return Collections.<String>emptyList();
    }

    @Override
    public List<Asset> getJavaScriptLibraries() {

        List<Asset> result = new ArrayList<>();

        result.add(assetSource.getContextAsset("vendor/jquery-1.11.2.min.js", null));
        result.add(assetSource.getContextAsset("vendor/jquery-ui/js/jquery-ui-1.10.3.custom.min.js", null));
        result.add(assetSource.getContextAsset("vendor/modernizr-2.6.2-respond-1.1.0.min.js", null));
        result.add(assetSource.getContextAsset("vendor/bootstrap/js/bootstrap.js", null));

        return result;
    }

    @Override
    public List<StylesheetLink> getStylesheets() {
        return Collections.<StylesheetLink>emptyList();
    }

    @Override
    public String getInitialization() {
        return null;
    }

}
