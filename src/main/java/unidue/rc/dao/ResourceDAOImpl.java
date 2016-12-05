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


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.NamedQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.*;
import unidue.rc.system.SystemConfigurationService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 20.11.13 09:02
 */
public class ResourceDAOImpl extends BaseDAOImpl implements ResourceDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceDAOImpl.class);

    private final File filesDir;

    public ResourceDAOImpl(@Inject SystemConfigurationService systemConfigurationService) {
        filesDir = new java.io.File(systemConfigurationService.getString("files.store"));
    }

    @Override
    public Resource createResourceFromStream(String filename, InputStream input, ResourceContainer container) throws
            CommitException, IOException {

        Resource resource = new Resource();
        create(resource);

        // extract data needed to build the file path
        Integer collectionID = container.getEntry().getReserveCollection().getId();

        // store the file
        File output = createOutputFile(collectionID, resource.getId(), filename);
        FileUtils.copyInputStreamToFile(input, output);

        // set meta data of the resource
        resource.setFilePath(buildFilePath(collectionID, resource.getId(), filename));
        resource.setCopyrightReviewStatus(CopyrightReviewStatus.NOT_REVIEWED);
        resource.setMimeType(detectMimeType(output));

        return resource;
    }

    @Override
    public java.io.File createOutputFile(Integer collectionID, Integer resourceID, String filename) throws IOException {

        String subdirPath = buildStoreDirectoryPath(collectionID, resourceID);

        java.io.File dir = new java.io.File(filesDir, subdirPath);
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("could not create directory " + dir.getAbsolutePath() + " for file " + filename);

        return new java.io.File(dir, filename);
    }

    @Override
    public String buildFilePath(Integer collectionID, Integer resourceID, String filename) {
        StringBuilder builder = new StringBuilder(buildStoreDirectoryPath(collectionID, resourceID));
        builder.append(java.io.File.separator);
        builder.append(filename);
        return builder.toString();
    }

    /**
     * Builds a relative path to collections home directory that should be used to store a file for an entry. The path
     * is just build as a string and is not created in file system structure
     *
     * @param collectionID collection id used as directory
     * @param resourceID   resource id used as directory
     * @return a string containing the relative directory path
     */
    private String buildStoreDirectoryPath(Integer collectionID, Integer resourceID) {
        StringBuilder builder = new StringBuilder();

        // build path like "143/5"
        builder.append(collectionID.toString());
        builder.append(java.io.File.separator);
        builder.append(resourceID.toString());
        return builder.toString();
    }

    @Override
    public String detectMimeType(java.io.File file) {
        try {

            Tika tika = new Tika();
            String mimetype = tika.detect(file);
            LOG.debug("detected mime type \"" + mimetype + "\" from file " + file);
            return mimetype;
        } catch (IOException e) {
            LOG.info("could not detect mime type of file " + file);
            return null;
        }
    }

    @Override
    public void rename(Resource resource, String filename) throws IOException, CommitException {
        File srcFile = new File(filesDir, resource.getFilePath());

        String srcPath = srcFile.getAbsolutePath();
        File destFile = new File(FilenameUtils.concat(FilenameUtils.getFullPath(srcPath), filename));

        if (srcFile.exists()) {
            FileUtils.moveFile(srcFile, destFile);

            String resourcePath = FilenameUtils.getPath(resource.getFilePath());
            String newPath = FilenameUtils.concat(resourcePath, FilenameUtils.getName(destFile.getPath()));
            resource.setFilePath(newPath);
            update(resource);
        }
    }

    @Override
    public FileDeleteStatus deleteFile(Resource resource) {

        if (StringUtils.isNotBlank(resource.getFilePath())) {

            java.io.File realFile = new java.io.File(filesDir, resource.getFilePath());
            if (realFile.exists()) {
                // file exists -> delete
                boolean deleted = realFile.delete();
                if (deleted) {
                    // set path to null as file was deleted
                    setNullPath(resource);
                    return FileDeleteStatus.Deleted;
                } else {
                    // file could not be delete (ex. permissions not given)
                    return FileDeleteStatus.NotDeleted;
                }
            } else {
                // file path is set but no file exists
                setNullPath(resource);
            }
        }
        return FileDeleteStatus.NoFile;
    }

    private void setNullPath(Resource resource) {

        resource.setFilePath(null);
        try {
            update(resource);
        } catch (CommitException e) {
            LOG.warn("could not null filepath of resource " + resource, e);
        }
    }

    @Override
    public List<Resource> getResourcesByCollection(ReserveCollection collection) {


        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_RESOURCES_BY_COLLECTION_QUERYNAME,
                Collections.singletonMap("collectionID", collection.getId()));

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Resource> resources = context.performQuery(query);
        return resources != null
                ? resources
                : Collections.EMPTY_LIST;
    }

    /**
     * Deletes target object from backend and any {@link java.io.File} that may be associated with it.
     */
    @Override
    public void delete(Persistent object) throws DeleteException {
        if (object instanceof Resource) {
            Resource resource = (Resource) object;
            deleteFile(resource);
        }
        super.delete(object);
    }
}
