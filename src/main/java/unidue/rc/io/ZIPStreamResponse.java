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
package unidue.rc.io;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.services.Response;

import java.io.*;
import java.util.List;

/**
 * Outputs a zip stream to any {@link OutputStream}. There is no temporary zip file created on disk, the zip is
 * directly written to the output.
 */
public class ZIPStreamResponse implements OutputStreamResponse {

    private final List<File> files;
    private final String zipResponseName;
    private final String zipRootFolderName;

    /**
     * Creates a new instance of this class using given names to arrange files in the zip.
     *
     * @param zipResponseName   pure name of the zip file, the suffix .zip is added automatically
     * @param zipRootFolderName root folder name where all files are put under
     * @param files             all files that must be put into the zip
     */
    public ZIPStreamResponse(String zipResponseName, String zipRootFolderName, List<File> files) {
        this.zipResponseName = FilenameUtils.getBaseName(zipResponseName) + ".zip";
        this.zipRootFolderName = zipRootFolderName;
        this.files = files;
    }

    @Override
    public String getContentType() {
        return "application/zip";
    }

    @Override
    public void processRequest(OutputStream out) throws IOException {

        /*
        Use apache commons compress to create the zip, as there are problems with umlauts when using the default
        java.util.zip classes.
         */
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out);
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        for (File file : files) {
            // name must include root folder, otherwise there are just files inside the zip
            String name = String.join(File.separator, zipRootFolderName, file.getName());
            ArchiveEntry entry = new ZipArchiveEntry(name);
            zos.putArchiveEntry(entry);

            // copy file to zip output
            try (InputStream input = new FileInputStream(file)) {

                if (file.length() > Integer.MAX_VALUE) {
                    IOUtils.copyLarge(input, zos);
                } else {
                    IOUtils.copy(input, zos);
                }
            } finally {
                zos.flush();
                zos.closeArchiveEntry();
            }
        }
        zos.flush();
        zos.close();
    }

    @Override
    public void prepareResponse(Response response) {

        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipResponseName + "\"");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
    }
}
