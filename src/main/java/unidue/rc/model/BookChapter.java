package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;
import unidue.rc.model.auto._BookChapter;

import java.util.Date;

public class BookChapter extends _BookChapter implements LibraryItem, Scannable, IntPrimaryKey, CollectionVisitable {

    @Override
    public Boolean isContentAvailable() {
        Resource resource = getResource();
        return resource != null
                && (resource.getFullTextURL() != null || resource.getFilePath() != null);
    }

    @Override
    public String getTitle() {
        return getChapterTitle();
    }

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    @Override
    public ReserveCollection getReserveCollection() {
        return getEntry().getReserveCollection();
    }

    @Override
    public Scannable clone() {
        BookChapter chapter = new BookChapter();
        chapter.setChapterTitle(getChapterTitle());
        chapter.setComment(getComment());
        chapter.setChapterAuthors(getChapterAuthors());
        chapter.setBookAuthors(getBookAuthors());
        chapter.setIsbn(getIsbn());
        chapter.setEditor(getEditor());
        chapter.setEdition(getEdition());
        chapter.setPlaceOfPublication(getPlaceOfPublication());
        chapter.setBookTitle(getBookTitle());
        chapter.setPageStart(getPageStart());
        chapter.setPageEnd(getPageEnd());
        chapter.setPublisher(getPublisher());
        chapter.setSignature(getSignature());
        chapter.setYearOfPublication(getYearOfPublication());
        chapter.setReferenceNumber(getReferenceNumber());
        return chapter;
    }

    @Override
    public boolean isFileAvailable() {
        Resource resource = getResource();
        return resource != null
                && resource.getFileDeleted() == null
                && StringUtils.isNotBlank(resource.getFilePath());
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);

        Resource resource = getResource();
        if (resource != null)
            resource.accept(visitor);

        visitor.didVisit(this);
    }

    @Override
    public Date getModified() {
        return getEntry().getModified();
    }
}
