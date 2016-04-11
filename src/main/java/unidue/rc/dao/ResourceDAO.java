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
package unidue.rc.dao;


import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.ResourceContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A <code>ResourceDAO</code> should be used when {@link Resource} objects are created, updated, modified or deleted. A
 * {@link java.io.File} is stored for every file entry inside the database. These files for example must be deleted if
 * the entry is deleted. All files are going to be stored in a directory relative to the home directory used for this
 * application and configured inside the <code>sysconfig.xml</code> via <code>collection.home</code> property. The
 * sub directory a file is stored in is <code>collection.home/files/collection.id/entry.id/filename</code>
 *
 * @author Nils Verheyen
 * @since 20.11.13 09:02
 */
public interface ResourceDAO extends BaseDAO {

    String SERVICE_NAME = "ResourceDAO";

    /**
     * Represents a status after a file should be deleted from a resource.
     */
    enum FileDeleteStatus {
        Deleted,
        NotDeleted,
        NoFile
    }

    /**
     * Creates a new {@link Resource} for target {@link ResourceContainer} that contains a {@link java.io.File} with
     * target name and input.
     *
     * @param filename  filename of the resource
     * @param input     input stream
     * @param container resource container that the resource belongs to
     * @return the saved resource
     * @throws CommitException when the {@link Resource} could not be created
     * @throws IOException     if storing of the new file could not be executed
     */
    Resource createResourceFromStream(String filename, InputStream input, ResourceContainer container) throws
            CommitException, IOException;

    /**
     * Creates a {@link java.io.File} for target file entry in backend. The file has to be associated to a {@link
     * unidue.rc.model.ReserveCollection} because the output file is created by the id of the collection and the file.
     *
     * @param collectionID reserve collection id
     * @param resourceID   resource id
     * @param filename     contains the filename that should be used for the output file
     * @return a new file as output sink
     * @throws IOException thrown if the directory for the file could not be created
     */
    java.io.File createOutputFile(Integer collectionID, Integer resourceID, String filename) throws IOException;

    /**
     * Builds the complete relative path to collections home that should be set via {@link
     * unidue.rc.model.Resource#setFilePath(String)} .
     *
     * @param collectionID collection id used as directory
     * @param resourceID   resource id used as directory
     * @param filename     filename with extension used for the file
     * @return a string containing the relative file path
     */
    String buildFilePath(Integer collectionID, Integer resourceID, String filename);

    /**
     * Tries to detect the mime type of target file.
     *
     * @param file contains the file object which mime type should be detected
     * @return a mimetype if one could be found, <code>null</code> otherwise
     */
    String detectMimeType(java.io.File file);

    /**
     * Deletes the {@link java.io.File} that is bound to target {@link unidue.rc.model.Resource} if it present,
     * otherwise not file is deleted.
     *
     * @param resource resource of the file
     * @return {@link FileDeleteStatus#Deleted} when the file was successfully deleted,
     * {@link FileDeleteStatus#NotDeleted} when the file was not delete and {@link FileDeleteStatus#NoFile} if the resource was
     * not associated with a resource.
     */
    FileDeleteStatus deleteFile(Resource resource);

    /**
     * Returns all {@link Resource} objects that are bound to target {@link ReserveCollection} through entries.
     *
     * @param collection collection for the resources
     * @return all resources used inside the collection or an empty list
     */
    List<Resource> getResourcesByCollection(ReserveCollection collection);
}
