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


import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;

/**
 * @author Nils Verheyen
 * @since 16.12.14 08:02
 */
public class LibraryLocationServiceImpl implements LibraryLocationService {

    private static final Logger LOG = LoggerFactory.getLogger(LibraryLocationServiceImpl.class);

    @Inject
    private LibraryLocationDAO locationDAO;

    @Inject
    private CollectionService collectionService;

    @Override
    public void create(LibraryLocation location) throws CommitException {
        locationDAO.create(location);
    }

    @Override
    public void update(LibraryLocation location) throws CommitException {
        locationDAO.update(location);
        collectionService.afterLocationUpdate(location);
    }

    @Override
    public void delete(LibraryLocation location) throws DeleteException {
        collectionService.beforeLocationDelete(location);
        locationDAO.delete(location);
        collectionService.afterLocationDelete(location);
    }
}
