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

/**
 * Created with IntelliJ IDEA.
 * User: mkoesters
 * Date: 24.09.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;
import unidue.rc.model.Resource;

import java.io.*;

public class AttachmentStreamResponse implements StreamResponse {

    private File file = null;
    private InputStream input = null;

    protected String contentType = "application/x-download";

    // protected String extension = "txt";

    protected String filename = "default";


    public AttachmentStreamResponse(File file, Resource resource) {
        this.file = file;
        this.filename = resource.getFileName();
        this.contentType = resource.getMimeType();
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getStream() throws IOException {
        if (input == null) {
            input = new BufferedInputStream(new FileInputStream(file));
        }
        return input;
    }

    public void prepareResponse(Response response) {

        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Content-Length", Long.toString(file.length()));
        //We can't set the length here because we only have an Input Stream at this point. (Although we'd like to.)
        //We can only get size from an output stream.  arg0.setContentLength(.length);
    }


}