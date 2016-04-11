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


import miless.model.User;
import unidue.rc.security.CollectionSecurityService;

/**
 * This enumeration represents all actions which are secured inside the application through access rights. Possible
 * values are:
 * <ul>
 *     <li>{@link #VIEW_RESERVE_COLLECTION}</li>
 *     <li>{@link #CREATE_RESERVE_COLLECTION}</li>
 *     <li>{@link #EDIT_RESERVE_COLLECTION}</li>
 *     <li>{@link #ACTIVATE_RESERVE_COLLECTION}</li>
 *     <li>{@link #DEACTIVATE_RESERVE_COLLECTION}</li>
 *     <li>{@link #ARCHIVE_RESERVE_COLLECTION}</li>
 *     <li>{@link #DELETE_RESERVE_COLLECTION}</li>
 *     <li>{@link #EDIT_RESERVE_COLLECTION_ADMIN_DATA}</li>
 *     <li>{@link #READ_COLLECTION_STATISTICS}</li>
 *     <li>{@link #EDIT_ENTRIES}</li>
 *     <li>{@link #EDIT_ASSISTANT_PARTICIPATION}</li>
 *     <li>{@link #EDIT_STUDENT_ROLE_KEY}</li>
 *     <li>{@link #EDIT_STUDENT_PARTICIPATION}</li>
 *     <li>{@link #EDIT_ROLES}</li>
 *     <li>{@link #EDIT_BOOK_JOBS}</li>
 *     <li>{@link #EDIT_SCAN_JOBS}</li>
 *     <li>{@link #EDIT_LOCATIONS}</li>
 * </ul>
 *
 * @see unidue.rc.model.Action
 * @see unidue.rc.model.Membership
 * @see CollectionSecurityService
 * @see <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Rechte">Rechte</a>
 */
public enum ActionDefinition {

    /**
     * Defines the action if a {@link miless.model.User} wants to view the contents of a {@link
     * unidue.rc.model.ReserveCollection}
     */
    VIEW_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.READ),

    /**
     * Defines the action if a {@link miless.model.User} wants to export the contents of a {@link
     * unidue.rc.model.ReserveCollection}
     */
    EXPORT_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.EXPORT),

    /**
     * Defines the action if a {@link miless.model.User} wants to create a new {@link unidue.rc.model.ReserveCollection}
     */
    CREATE_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.CREATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit the metadata of a {@link
     * unidue.rc.model.ReserveCollection}. Do not mix up with {@link #EDIT_ENTRIES}.
     */
    EDIT_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to activate a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    ACTIVATE_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.ACTIVATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to renew a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    RENEW_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.RENEW),

    /**
     * Defines the action if a {@link miless.model.User} wants to prolong a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    PROLONG_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.PROLONG),

    /**
     * Defines the action if a {@link miless.model.User} wants to deactivate a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    DEACTIVATE_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.DEACTIVATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to archive a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    ARCHIVE_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.ARCHIVE),

    /**
     * Defines the action if a {@link miless.model.User} wants to delete a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    DELETE_RESERVE_COLLECTION(Resource.RESERVE_COLLECTION, Name.DELETE),

    /**
     * Defines the action if a {@link miless.model.User} wants to migrate one or more legacy {@link
     * unidue.rc.model.ReserveCollection}s.
     */
    MIGRATE_OLD_COLLECTIONS(Resource.MIGRATE, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit administrative data of a {@link
     * unidue.rc.model.ReserveCollection} like aleph system user.
     */
    EDIT_RESERVE_COLLECTION_ADMIN_DATA(Resource.COLLECTION_ADMIN_DATA, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit the {@link ReserveCollectionNumber} of a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    EDIT_RESERVE_COLLECTION_NUMBER(Resource.RESERVE_COLLECTION_NUMBER, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to view the statistics of a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    READ_COLLECTION_STATISTICS(Resource.COLLECTION_STATISTICS, Name.READ),

    /**
     * Defines the action if a {@link User} is able to filter the list of collections by status.
     */
    FILTER_COLLECTION_LIST_BY_STATUS(Resource.COLLECTION_STATUS_FILTER, Name.READ),

    /**
     * Defines the action if a {@link User} is able to edit the location of a collection
     * @see ReserveCollection#getLibraryLocation()
     */
    EDIT_RESERVE_COLLECTION_LOCATION(Resource.COLLECTION_LOCATION, Name.UPDATE),

    /**
     * Defines the action if a {@link User} is able to edit the expiration date of a reserve collection
     * @see ReserveCollection#getValidTo()
     */
    EDIT_RESERVE_COLLECTION_EXPIRATION(Resource.COLLECTION_EXPIRATION_DATE, Name.UPDATE),

    /**
     * Defines the action if a {@link User} is able to view the expiration date of a reserve collection
     * @see ReserveCollection#getValidTo()
     */
    VIEW_RESERVE_COLLECTION_EXPIRATION(Resource.COLLECTION_EXPIRATION_DATE, Name.READ),

    /**
     * Defines the action if a {@link miless.model.User} wants to create or edit specific entries of a {@link
     * unidue.rc.model.ReserveCollection}.
     */
    EDIT_ENTRIES(Resource.ENTRIES, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit the meta data of a {@link Book}.
     */
    EDIT_BOOK_META_DATA(Resource.BOOK_META_DATA, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit {@link unidue.rc.model.Participation}s of
     * users with the {@link Role} {@link unidue.rc.model.DefaultRole#ASSISTANT} in a {@link
     * unidue.rc.model.ReserveCollection} .
     */
    EDIT_ASSISTANT_PARTICIPATION(Resource.ASSISTANT_PARTICIPATION, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to add himself with the rights of a
     * {@link unidue.rc.model.DefaultRole#ASSISTANT} to a {@link
     * unidue.rc.model.ReserveCollection} .
     */
    EDIT_ASSISTANT_ROLE_KEY(Resource.ASSISTANT_ROLE_KEY, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit the {@link unidue.rc.model.CopyrightReviewStatus}
     * of a {@link unidue.rc.model.Resource}.
     */
    EDIT_COPYRIGHT_REVIEWS(Resource.COPYRIGHT_REVIEWS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit {@link unidue.rc.model.Participation}s of
     * users with the {@link Role} {@link unidue.rc.model.DefaultRole#DOCENT} in a {@link
     * unidue.rc.model.ReserveCollection} .
     */
    EDIT_DOCENT_PARTICIPATION(Resource.DOCENT_PARTICIPATION, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to add himself with the rights of a
     * {@link unidue.rc.model.DefaultRole#STUDENT} to a {@link
     * unidue.rc.model.ReserveCollection} .
     */
    EDIT_STUDENT_ROLE_KEY(Resource.STUDENT_ROLE_KEY, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit {@link unidue.rc.model.Participation}s of
     * users with the {@link Role} {@link unidue.rc.model.DefaultRole#STUDENT} in a {@link
     * unidue.rc.model.ReserveCollection} .
     */
    EDIT_STUDENT_PARTICIPATION(Resource.STUDENT_PARTICIPATION, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to delete his or her own
     * {@link unidue.rc.model.Participation} in a {@link unidue.rc.model.ReserveCollection}
     */
    DELETE_OWN_PARTICIPATION(Resource.OWN_PARTICIPATION, Name.DELETE),

    /**
     * Defines the action if a {@link miless.model.User} wants to reenter the access key of of a
     * {@link unidue.rc.model.ReserveCollection} to update the participation
     */
    REENTER_ACCESS_KEY(Resource.ACCESS_KEY, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit action that can be executed with a specific
     * {@link unidue.rc.model.Role}.
     */
    EDIT_ROLES(Resource.ROLE, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit {@link unidue.rc.model.Membership}s of a
     * {@link miless.model.User}
     */
    EDIT_USER_ROLES(Resource.MEMBERSHIP, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to manage book jobs
     */
    EDIT_BOOK_JOBS(Resource.BOOK_JOBS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to manage scan jobs
     */
    EDIT_SCAN_JOBS(Resource.SCAN_JOBS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to manage {@link unidue.rc.model.LibraryLocation}s
     */
    EDIT_LOCATIONS(Resource.LOCATIONS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit application settings.
     */
    EDIT_SETTINGS(Resource.SETTINGS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit administrative data of collections.
     */
    EDIT_COLLECTION_SETTINGS(Resource.COLLECTION_SETTINGS, Name.UPDATE),

    /**
     * Defines the action if a {@link miless.model.User} wants to edit user details
     */
    EDIT_USER(Resource.USER, Name.UPDATE)
    ;

    private String resource;
    private String name;

    ActionDefinition(String resource, String name) {
        this.resource = resource;
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public String getName() {
        return name;
    }

    public static class Resource {

        public static final String RESERVE_COLLECTION = "reserve-collection";
        public static final String RESERVE_COLLECTION_NUMBER = "reserve-collection-number";
        public static final String COLLECTION_ADMIN_DATA = "collection-admin-data";
        public static final String COLLECTION_STATISTICS = "collection-stats";
        public static final String COLLECTION_STATUS_FILTER = "collection-status-filter";
        public static final String COLLECTION_LOCATION = "collection-location";
        public static final String COLLECTION_EXPIRATION_DATE = "collection-expiration-date";
        public static final String ENTRIES = "entries";
        public static final String BOOK_META_DATA = "book-meta-data";
        public static final String ASSISTANT_PARTICIPATION = "assistant-participation";
        public static final String ASSISTANT_ROLE_KEY = "assistant-role-key";
        public static final String COPYRIGHT_REVIEWS = "copyright-reviews";
        public static final String DOCENT_PARTICIPATION = "docent-participation";
        public static final String STUDENT_ROLE_KEY = "student-role-key";
        public static final String STUDENT_PARTICIPATION = "student-participation";
        public static final String OWN_PARTICIPATION = "own-participation";
        public static final String ACCESS_KEY = "access-key";
        public static final String ROLE = "roles";
        public static final String MEMBERSHIP = "membership";
        public static final String BOOK_JOBS = "book-jobs";
        public static final String SCAN_JOBS = "scan-jobs";
        public static final String LOCATIONS = "locations";
        public static final String SETTINGS = "settings";
        public static final String COLLECTION_SETTINGS = "collection-settings";
        public static final String MIGRATE = "migrate";
        public static final String USER = "user";
    }

    public static class Name {

        public static final String CREATE = "create";
        public static final String READ = "read";
        public static final String UPDATE = "update";
        public static final String DELETE = "delete";
        public static final String EXPORT = "export";
        public static final String ARCHIVE = "archive";
        public static final String ACTIVATE = "activate";
        public static final String DEACTIVATE = "deactivate";
        public static final String RENEW = "renew";
        public static final String PROLONG = "prolong";
    }
}
