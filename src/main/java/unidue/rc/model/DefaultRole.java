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


import org.apache.cayenne.ExtendedEnumeration;

/**
 * The default roles that this application uses to determine access rights to specific objects or general actions.
 *
 * @author Nils Verheyen
 * @since 18.08.14 10:21
 */
public enum DefaultRole implements ExtendedEnumeration {

    ADMINISTRATOR(Names.ADMINISTRATOR),
    COLLECTION_ADMIN(Names.COLLECTION_ADMINISTRATOR),
    DOCENT(Names.DOCENT),
    ASSISTANT(Names.ASSISTANT),
    STUDENT(Names.STUDENT);

    public final String name;

    DefaultRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object getDatabaseValue() {
        return name;
    }

    public static class Names {

        public static final String ADMINISTRATOR = "Administrator";
        public static final String COLLECTION_ADMINISTRATOR = "Semapp Admin";
        public static final String DOCENT = "Dozent";
        public static final String ASSISTANT = "Bearbeiter";
        public static final String STUDENT= "Leser";
    }
}
