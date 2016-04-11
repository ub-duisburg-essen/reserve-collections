package unidue.rc.model;


/**
 * Created by nils on 06.08.15.
 */
public interface CollectionVisitor {

    void visit(ReserveCollection collection);
    void didVisit(ReserveCollection collection);

    void visit(Participation participation);

    void visit(LibraryLocation location);

    void visit(Entry entry);
    void didVisit(Entry entry);

    // simple entries

    void visit(Html html);

    void visit(Headline headline);

    void visit(WebLink webLink);


    // resource container

    void visit(Reference reference);
    void didVisit(Reference reference);

    void visit(File file);
    void didVisit(File file);

    void visit(JournalArticle article);
    void didVisit(JournalArticle article);

    void visit(BookChapter chapter);
    void didVisit(BookChapter chapter);

    void visit(Book book);
    void didVisit(Book book);

    void visit(Resource resource);

    void startList(String fieldName);
    void endList();

}
