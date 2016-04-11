package unidue.rc.model;


import org.apache.commons.io.FilenameUtils;
import unidue.rc.model.auto._Resource;

import java.io.File;

public class Resource extends _Resource implements IntPrimaryKey, CollectionVisitable {

    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    public String getFileName() {
        String path = getFilePath();
        return path != null
               ? FilenameUtils.getName(path)
               : null;
    }

    public String getExtension() {
        String path = getFilePath();
        return path != null
               ? FilenameUtils.getExtension(path)
               : null;
    }

    public ResourceContainer getResourceContainer() {
        return getJournalArticle() != null
                ? getJournalArticle()
                : getBookChapter() != null
                    ? getBookChapter()
                    : getFile() != null
                        ? getFile()
                        : getBook() != null
                            ? getBook()
                            : null;
    }

    public Entry getEntry() {
        return getResourceContainer() != null ? getResourceContainer().getEntry() : null;
    }

    public File getFile(File basedir) {
        return new java.io.File(basedir, getFilePath());
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
