package unidue.rc.ui.pages.collection;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.io.CollectionJsonWriter;
import unidue.rc.io.JSONStreamResponse;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by nils on 07.08.15.
 */
@Import(library = {
        "context:vendor/highlight-js/highlight.min.js",
        "context:js/entry/file/text.js",
}, stylesheet = {
        "context:vendor/highlight-js/default.min.css"
})
@BreadCrumb(titleKey = "collection.export.json")
@ProtectedPage
public class Json {

    @Inject
    private Logger log;

    @Inject
    private Response response;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Property
    private ReserveCollection collection;

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EXPORT_RESERVE_COLLECTION)
    Object onActivate(int collectionID) {

        collection = baseDAO.get(ReserveCollection.class, collectionID);
        if (collection == null)
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, "collection not found");

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("CollectionModule");
        module.addSerializer(ReserveCollection.class, new CollectionJsonWriter());
        mapper.registerModule(module);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        StringWriter stringWriter = new StringWriter();

        try {
            writer.writeValue(stringWriter, collection);
            StreamResponse jsonResponse = new JSONStreamResponse(stringWriter.toString());
            return jsonResponse;
        } catch (IOException e) {
            log.error("could not write collection as json " + collection.getId(), e);
        }
        return null;
    }
}
