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
package unidue.rc.model;


/**
 * Created by nils on 03.06.15.
 */
public enum DefaultPermissionDefinition {

    // Docent default permissions
    DOCENT_READ_COLLECTION(ActionDefinition.VIEW_RESERVE_COLLECTION, DefaultRole.DOCENT, true),
    DOCENT_CREATE_COLLECTION(ActionDefinition.CREATE_RESERVE_COLLECTION, DefaultRole.DOCENT, false),
    DOCENT_EDIT_RESERVE_COLLECTION(ActionDefinition.EDIT_RESERVE_COLLECTION, DefaultRole.DOCENT, true),
    DOCENT_RENEW_RESERVE_COLLECTION(ActionDefinition.RENEW_RESERVE_COLLECTION, DefaultRole.DOCENT, true),
    DOCENT_DEACTIVATE_RESERVE_COLLECTION(ActionDefinition.DEACTIVATE_RESERVE_COLLECTION, DefaultRole.DOCENT, true),
    DOCENT_PROLONG_RESERVE_COLLECTION(ActionDefinition.PROLONG_RESERVE_COLLECTION, DefaultRole.DOCENT, false),
    DOCENT_EDIT_DOCENT_PARTICIPATION(ActionDefinition.EDIT_DOCENT_PARTICIPATION, DefaultRole.DOCENT, true),
    DOCENT_EDIT_ASSISTANT_PARTICIPATION(ActionDefinition.EDIT_ASSISTANT_PARTICIPATION, DefaultRole.DOCENT, true),
    DOCENT_EDIT_ASSISTANT_ROLE_KEY(ActionDefinition.EDIT_ASSISTANT_ROLE_KEY, DefaultRole.DOCENT, true),
    DOCENT_EDIT_STUDENT_PARTICIPATION(ActionDefinition.EDIT_STUDENT_PARTICIPATION, DefaultRole.DOCENT, true),
    DOCENT_EDIT_STUDENT_ROLE_KEY(ActionDefinition.EDIT_STUDENT_ROLE_KEY, DefaultRole.DOCENT, true),
    DOCENT_EDIT_ENTRIES(ActionDefinition.EDIT_ENTRIES, DefaultRole.DOCENT, true),
    DOCENT_READ_COLLECTION_STATISTICS(ActionDefinition.READ_COLLECTION_STATISTICS, DefaultRole.DOCENT, true),
    DOCENT_FILTER_COLLECTIONS_BY_STATUS(ActionDefinition.FILTER_COLLECTION_LIST_BY_STATUS, DefaultRole.DOCENT, false),

    // Assistant default permissions
    ASSISTANT_READ_COLLECTION(ActionDefinition.VIEW_RESERVE_COLLECTION, DefaultRole.ASSISTANT, true),
    ASSISTANT_EDIT_RESERVE_COLLECTION(ActionDefinition.EDIT_RESERVE_COLLECTION, DefaultRole.ASSISTANT, true),
    ASSISTANT_PROLONG_RESERVE_COLLECTION(ActionDefinition.PROLONG_RESERVE_COLLECTION, DefaultRole.ASSISTANT, false),
    ASSISTANT_RENEW_RESERVE_COLLECTION(ActionDefinition.RENEW_RESERVE_COLLECTION, DefaultRole.ASSISTANT, true),
    ASSISTANT_DEACTIVATE_RESERVE_COLLECTION(ActionDefinition.DEACTIVATE_RESERVE_COLLECTION, DefaultRole.ASSISTANT, true),
    ASSISTANT_EDIT_DOCENT_PARTICIPATION(ActionDefinition.EDIT_DOCENT_PARTICIPATION, DefaultRole.DOCENT, true),
    ASSISTANT_EDIT_ASSISTANT_PARTICIPATION(ActionDefinition.EDIT_ASSISTANT_PARTICIPATION, DefaultRole.DOCENT, true),
    ASSISTANT_EDIT_ASSISTANT_ROLE_KEY(ActionDefinition.EDIT_ASSISTANT_ROLE_KEY, DefaultRole.DOCENT, true),
    ASSISTANT_EDIT_STUDENT_PARTICIPATION(ActionDefinition.EDIT_STUDENT_PARTICIPATION, DefaultRole.ASSISTANT, true),
    ASSISTANT_EDIT_STUDENT_ROLE_KEY(ActionDefinition.EDIT_STUDENT_ROLE_KEY, DefaultRole.ASSISTANT, true),
    ASSISTANT_EDIT_ENTRIES(ActionDefinition.EDIT_ENTRIES, DefaultRole.ASSISTANT, true),
    ASSISTANT_READ_COLLECTION_STATISTICS(ActionDefinition.READ_COLLECTION_STATISTICS, DefaultRole.ASSISTANT, true),
    ASSISTANT_DELETE_OWN_PARTICIPATION(ActionDefinition.DELETE_OWN_PARTICIPATION, DefaultRole.ASSISTANT, true),
    ASSISTANT_FILTER_COLLECTIONS_BY_STATUS(ActionDefinition.FILTER_COLLECTION_LIST_BY_STATUS, DefaultRole.ASSISTANT, false),

    // Student default permissions
    STUDENT_READ_COLLECTION(ActionDefinition.VIEW_RESERVE_COLLECTION, DefaultRole.STUDENT, true),
    STUDENT_DELETE_OWN_PARTICIPATION(ActionDefinition.DELETE_OWN_PARTICIPATION, DefaultRole.STUDENT, true),
    STUDENT_REENTER_ACCESS_KEY(ActionDefinition.REENTER_ACCESS_KEY, DefaultRole.STUDENT, true),
    STUDENT_FILTER_COLLECTIONS_BY_STATUS(ActionDefinition.FILTER_COLLECTION_LIST_BY_STATUS, DefaultRole.STUDENT, false),
    ;

    private ActionDefinition action;
    private DefaultRole role;
    private boolean isInstanceBound;

    DefaultPermissionDefinition(ActionDefinition action, DefaultRole role, boolean isInstanceBound) {
        this.action = action;
        this.role = role;
        this.isInstanceBound = isInstanceBound;
    }

    public ActionDefinition getAction() {
        return action;
    }

    public DefaultRole getRole() {
        return role;
    }

    public boolean isInstanceBound() {
        return isInstanceBound;
    }
}
