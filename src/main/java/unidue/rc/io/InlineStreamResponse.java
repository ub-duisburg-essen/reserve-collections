
package unidue.rc.io;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;
import unidue.rc.model.Resource;

import java.io.*;

/**
 * An <code>InlineStreamResponse</code> can be used to deliver a file for example to a browser and let the client choose
 * which action should be done with the file.
 *
 * @author Nils Verheyen
 * @since 30.07.14 11:12
 */
public class InlineStreamResponse implements StreamResponse {
    private File file = null;
    private InputStream is = null;

    protected Resource resource;

    public InlineStreamResponse(File file, Resource resource) {
        this.file = file;
        this.resource = resource;
    }

    public String getContentType() {
        return resource.getMimeType();
    }

    public InputStream getStream() throws IOException {
        if (is == null) {
            is = new BufferedInputStream(new FileInputStream(file));
        }
        return is;
    }

    public void prepareResponse(Response response) {

        String filename = FilenameUtils.getName(resource.getFilePath());
        response.setHeader("Content-Disposition", "inline; filename='" + filename + "'");
    }
}
