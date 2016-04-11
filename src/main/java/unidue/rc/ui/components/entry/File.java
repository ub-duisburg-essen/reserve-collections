package unidue.rc.ui.components.entry;


import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Entry;
import unidue.rc.workflow.ResourceService;

/**
 * Created by nils on 05.08.15.
 */
public class File {

    @Parameter(required = true, allowNull = false)
    @Property
    private unidue.rc.model.File file;

    @Inject
    private AssetSource assetSource;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    /**
     * Returns a filename of an icon that matches the mimetype of the current {@link Entry} if it is a {@link unidue.rc.model.File}. The
     * images can be found in webapp/img/mimetypes.
     *
     * @return the filename for target mime or the default
     */
    public String getFilenameForMime() {

        unidue.rc.model.Resource resource = resourceDAO.get(unidue.rc.model.Resource.class,
                file.getResource().getId());
        String mimeFilename = "";
        if (resource.getMimeType() != null) {

            mimeFilename = resource.getMimeType().replace("/", "-") + ".png";
        }
        Resource asset = assetSource.resourceForPath("context:img/mimetypes/" + mimeFilename);
        return asset.exists()
                ? asset.getFile()
                : getDefaultMimeImage(mimeFilename);
    }

    private String getDefaultMimeImage(String mimeFilename) {
        if (mimeFilename.startsWith("video"))
            return "media-video.png";
        else if (mimeFilename.startsWith("audio"))
            return "media-audio.png";
        else if (mimeFilename.startsWith("image"))
            return "media-image.png";
        else if (mimeFilename.startsWith("text"))
            return "text-plain.png";
        else
            return "unknown.png";
    }

    public boolean isDownloadAllowed() {
        return resourceService.isDownloadAllowed(file.getResource());
    }
}
