package unidue.rc.dao;

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

import org.apache.cayenne.*;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 12.03.14 11:18
 */
public class BaseDAOImpl implements BaseDAO {

    /**
     * Lock object used when creating new objects in {@link unidue.rc.dao.BaseDAOImpl#create(Object)}.
     */
    private static final String CREATE_LOCK = "CREATE_LOCK";

    private static final Logger LOG = LoggerFactory.getLogger(BaseDAOImpl.class);

    @Override
    public void create(Object o) throws CommitException {

        synchronized (CREATE_LOCK) {
            // get current ObjectContext
            ObjectContext objectContext = BaseContext.getThreadObjectContext();

            // register new entry so it is going to be persisted to
            // the backend
            objectContext.registerNewObject(o);

            try {
                objectContext.commitChanges();

                LOG.info("created object: " + o);
            } catch (CayenneRuntimeException e) {
                LOG.error("could not persist object: " + o, e);
                objectContext.rollbackChanges();
                throw new CommitException("could not create object " + o, e);
            }
        }
    }

    @Override
    public void update(Object o) throws CommitException {

        if (o instanceof CayenneDataObject) {
            CayenneDataObject dataObject = (CayenneDataObject) o;

            ObjectContext objectContext = dataObject.getObjectContext();// same as BaseContext.getThreadObjectContext();
            try {
                objectContext.commitChanges();
                LOG.info("object " + dataObject.getObjectId() + " updated");
            } catch (ValidationException e) {
                LOG.error("could not update object: " + dataObject + "; " + e.getMessage());
                objectContext.rollbackChanges();
                throw new CommitException("could not update data object " + dataObject, e);
            }
        }
    }

    @Override
    public void delete(Persistent object) throws DeleteException {
        ObjectContext context = BaseContext.getThreadObjectContext();
        ObjectId objectId = object.getObjectId();
        try {
            // new objects do not belong to object contexts
            if (context != null && objectId != null) {
                Object objectToDelete = Cayenne.objectForPK(context, objectId);

                // object may already be deleted for example because of cascading rules
                if (objectToDelete != null) {
                    context.deleteObject(objectToDelete);
                    LOG.info("object " + objectId + " deleted");
                } else {
                    LOG.warn("object to delete with id " + objectId + " not found");
                }
                context.commitChanges();
            }
        } catch (ValidationException e) {
            LOG.error("could not delete object " + object, e);
            context.rollbackChanges();
            throw new DeleteException("could not delete object " + object, e);
        }
    }

    @Override
    public <T> T get(Class<T> className, Integer id) {
        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(), className, id);
    }

    @Override
    public <T> List<T> get(Class<T> className, int offset, int maxResults) {

        if (offset < 0)
            throw new IllegalArgumentException("invalid offset of " + offset);
        if (maxResults > MAX_RESULTS || maxResults < 0)
            throw new IllegalArgumentException("max results must be between " + MAX_RESULTS + " and 0 but was " + maxResults);

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(className);
        query.setFetchLimit(maxResults);
        query.setFetchOffset(offset);

        List<T> results = context.performQuery(query);
        return results == null ? Collections.EMPTY_LIST : results;
    }
}
