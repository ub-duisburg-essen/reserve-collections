package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;
import unidue.rc.model.auto._JournalArticle;

import java.util.Date;

public class JournalArticle extends _JournalArticle implements LibraryItem, Scannable, IntPrimaryKey, CollectionVisitable {

    @Override
    public Boolean isContentAvailable() {
        Resource resource = getResource();
        return resource != null
                && (resource.getFullTextURL() != null || resource.getFilePath() != null);
    }

    @Override
    public boolean isFileAvailable() {
        Resource resource = getResource();
        return resource != null
                && resource.getFileDeleted() == null
                && StringUtils.isNotBlank(resource.getFilePath());
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
        JournalArticle article = new JournalArticle();
        article.setArticleTitle(getArticleTitle());
        article.setComment(getComment());
        article.setAuthors(getAuthors());
        article.setIssn(getIssn());
        article.setIssue(getIssue());
        article.setPlaceOfPublication(getPlaceOfPublication());
        article.setJournalTitle(getJournalTitle());
        article.setPageStart(getPageStart());
        article.setPageEnd(getPageEnd());
        article.setPublisher(getPublisher());
        article.setSignature(getSignature());
        article.setVolume(getVolume());
        article.setReferenceNumber(getReferenceNumber());
        return article;
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

    @Override
    public String getTitle() {
        return getArticleTitle();
    }
}
