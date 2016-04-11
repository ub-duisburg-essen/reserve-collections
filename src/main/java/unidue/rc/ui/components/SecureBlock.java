
package unidue.rc.ui.components;

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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractConditional;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.model.ActionDefinition;
import unidue.rc.security.CollectionSecurityService;

/**
 * A <code>SecureBlock</code> can be used to block certain parts inside the ui to forbid access to content. With a
 * secure block a value of {@link ActionDefinition} must be given to target a {@link unidue.rc.model.Action}
 * that should be executed
 *
 * @author Nils Verheyen
 * @since 21.08.14 09:54
 */
public class SecureBlock extends AbstractConditional {

    /**
     * Contains the action name which must be provided to check if the body of this block should be rendered.
     *
     * @see ActionDefinition
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private ActionDefinition action;

    @Parameter(name = "objectID", required = false)
    private Integer objectID;

    @Inject
    private CollectionSecurityService securityService;

    @Override
    protected boolean test() {
        return objectID != null
                ? securityService.isPermitted(action, objectID)
                : securityService.isPermitted(action);
    }
}
