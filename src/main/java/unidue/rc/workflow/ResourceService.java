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
package unidue.rc.workflow;


import miless.model.User;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.ResourceContainer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by marcus.koesters on 01.08.14.
 */
public interface ResourceService {

    /**
     * Returns <code>true</code> if the download of the file in target resouce is allowed.
     *
     * @param resource resource to check
     * @return <code>true</code> if download is allowed
     */
    boolean isDownloadAllowed(Resource resource);

    /**
     * Retrieves the file that belongs to target {@link Resource} for current logged in user that wants to perform the
     * download.
     *
     * @param resource contains the accessed resource
     * @return The file to the resource or null if it does not exist
     */
    java.io.File download(Resource resource);

    /**
     * Retrieves the file that belongs to target {@link Resource} for target {@link User} that wants execute a download.
     *
     * @param resource contains the accessed resource
     * @param user     contains the user that wants to download the file.
     * @return The file to the resource or null if it does not exist
     */
    java.io.File download(Resource resource, User user);

    /**
     * Creates a new {@link Resource} that points to a file with target filename, stored from target input.
     *
     * @param filename  filename for the new file
     * @param input     input stream
     * @param container container for the new resource
     * @return the new created resource
     * @throws CommitException when the resource could not be saved in backend
     * @throws IOException     when the file could not saved on hard disk
     */
    Resource create(String filename, InputStream input, ResourceContainer container) throws CommitException,
            IOException;

    /**
     * Creates a new {@link Resource} that points to target url.
     *
     * @param url       url for the resource
     * @param container container for the new resource
     * @return the new created resource
     * @throws CommitException when the resource could not be saved in backend
     */
    Resource create(String url, ResourceContainer container) throws CommitException;

    /**
     * Creates a new {@link Resource} that points to target url.
     *
     * @param url url for the resource
     * @return the new created resource
     * @throws CommitException when the resource could not be saved in backend
     */
    Resource create(String url) throws CommitException;

    /**
     * Uploads target input to a new resource with given filename
     *
     * @param collection collection for the resource
     * @param filename   filename for the new file
     * @param input      input stream
     * @return the new created resource
     * @throws CommitException when the resource could not be saved in backend
     * @throws IOException     when the file could not saved on hard disk
     */
    Resource upload(ReserveCollection collection, String filename, InputStream input) throws CommitException, IOException;

    /**
     * Updates target {@link Resource} and saves target input in a file. Old files are deleted if present.
     *
     * @param resource resource to update
     * @param filename Contains the filename that should be used
     * @param input    input source
     * @throws CommitException when the resource could not be saved in backend
     * @throws IOException     when the file could not saved on hard disk
     */
    void update(Resource resource, String filename, InputStream input) throws CommitException, IOException;

    /**
     * Updates target {@link Resource} in backend.
     *
     * @param resource resource to update
     * @throws CommitException when the resource could not be saved in backend
     */
    void update(Resource resource) throws CommitException;

    /**
     * Deletes target resource in backend and any associated files.
     *
     * @param resource resource to delete
     * @throws DeleteException when the resource or its file could not be deleted
     */
    void delete(Resource resource) throws DeleteException;

    void afterCollectionUpdate(ReserveCollection collection);

    void beforeEntryDelete(Entry entry);

    void afterEntryDelete(Entry entry);

    void setFileDeleted(Resource resource) throws CommitException;

    ResourceDAO.FileDeleteStatus deleteFile(Resource resource) throws CommitException;
}
