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
import unidue.rc.security.CollectionSecurityService;

/**
 * A <code>SecurityContextPage</code> is a tapestry page that displays details of parts of a
 * {@link unidue.rc.model.ReserveCollection}.
 * <p>
 * Created by nils on 11.06.15.
 */
public interface SecurityContextPage {

    /**
     * Checks if current user is authorized to view target page.
     *
     * @param securityService   contains the security service that should be used to check authorization
     * @param activationContext contains the activation context that will be used on page activation ({@link org.apache.tapestry5.EventConstants#ACTIVATE})
     * @throws AuthorizationException thrown if the user is not authorized
     */
    void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException;
}
