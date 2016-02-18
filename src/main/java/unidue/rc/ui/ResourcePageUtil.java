package unidue.rc.ui;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventContext;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;

/**
 * Created by nils on 18.06.15.
 */
public class ResourcePageUtil {

    public static void checkPermission(CollectionSecurityService securityService, EventContext activationContext, ResourceDAO resourceDAO) throws AuthorizationException {
        Integer resourceID = activationContext.get(Integer.class, 0);
        Resource resource = resourceDAO.get(Resource.class, resourceID);
        Integer collectionID = resource.getEntry().getReserveCollection().getId();
        securityService.checkPermission(ActionDefinition.VIEW_RESERVE_COLLECTION, collectionID);
    }
}
