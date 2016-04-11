package unidue.rc.migration.legacymodel;


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
