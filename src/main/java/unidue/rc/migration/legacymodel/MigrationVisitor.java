package unidue.rc.migration.legacymodel;

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

import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

/**
 * @author Nils Verheyen
 * @since 07.04.14 15:42
 */
public interface MigrationVisitor {

    void migrate(ArticleLocal article, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException;

    void migrate(BookLocal book, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException;

    void migrate(ChapterLocal chapter, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException;

    void migrate(FileLocal file, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException;

    void migrateHeadline(String headline, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID);

    void migrateHtml(String html, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID);

    void migrateText(FreeTextLocal text, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID);

    void migrate(WeblinkLocal weblink, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID);

    void migrate(DocumentLinkLocal documentLink, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID);
}
