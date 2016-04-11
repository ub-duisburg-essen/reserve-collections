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


import org.apache.cayenne.di.Inject;
import org.apache.tika.mime.MediaType;
import unidue.rc.model.Resource;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.pages.entry.file.Audio;
import unidue.rc.ui.pages.entry.file.Image;
import unidue.rc.ui.pages.entry.file.Text;
import unidue.rc.ui.pages.entry.file.Video;

import java.util.Collections;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 26.08.14 11:49
 */
public class MimeServiceImpl implements MimeService {

    @Inject
    private SystemConfigurationService config;

    @Override
    public Class<?> getPage(Resource resource) {
        String type = MediaType.parse(resource.getMimeType()).getType();

        // lookup by mime type
        switch (type) {
            case "video":
                return Video.class;
            case "audio":
                return Audio.class;
            case "image":
                return Image.class;
            case "text":
                return Text.class;
        }
        // lookup by extension
        String extension = resource.getExtension();
        if (extension != null) {
            List<String> extensionList = config.getStringArray("syntax.highlight.extensions");
            if (extensionList.contains(extension))
                return Text.class;
        }

        return null;
    }

    @Override
    public List<String> getStreamingProtocols(String mimeType) {
        MediaType mediaType = MediaType.parse(mimeType);
        return mediaType != null
                ? config.getStringArray("stream.format.protocol." + mediaType.getSubtype())
                : Collections.<String>emptyList();
    }

    @Override
    public Integer getStreamingPort(String protocol) {
        return config.getInt("stream.protocol.port." + protocol);
    }
}
