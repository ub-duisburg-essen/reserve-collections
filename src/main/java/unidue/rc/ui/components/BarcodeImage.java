package unidue.rc.ui.components;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.OneDimensionalCodeWriter;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Nils Verheyen
 * @since 18.12.13 11:02
 */
@SupportsInformalParameters
public class BarcodeImage {

    @Parameter(allowNull = false, required = true)
    private String content;

    @Parameter(value = "256")
    private int width;

    @Parameter(value = "100")
    private int height;

    @Inject
    private ComponentResources resources;

    @Inject
    private Response response;

    @BeginRender
    boolean beginRender(MarkupWriter writer) {
        Link link = resources.createEventLink("image");
        writer.element("img",
                "src", link.toAbsoluteURI(),
                "width", width,
                "height", height);

        resources.renderInformalParameters(writer);

        writer.end();

        return false;
    }

    void onImage() throws IOException, WriterException {

        OneDimensionalCodeWriter codeWriter = new Code128Writer();
        final BitMatrix matrix;
        matrix = codeWriter.encode(content, BarcodeFormat.CODE_128, width, height);

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        OutputStream stream = response.getOutputStream("image/jpeg");

        ImageIO.write(image, "jpg", stream);

        stream.flush();

        stream.close();
    }
}
