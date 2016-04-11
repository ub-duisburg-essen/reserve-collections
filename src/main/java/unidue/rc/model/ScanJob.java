package unidue.rc.model;


import unidue.rc.model.auto._ScanJob;

public class ScanJob extends _ScanJob implements IntPrimaryKey {

    private Scannable scannable;

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }

    public void setScannable(Scannable scannable) {
        if (scannable instanceof BookChapter)
            setBookChapter((BookChapter) scannable);
        else if (scannable instanceof JournalArticle)
            setJournalArticle((JournalArticle) scannable);
    }

    public Scannable getScannable() {
        return getBookChapter() != null
                ? getBookChapter()
                : getJournalArticle() != null
                    ? getJournalArticle()
                    : null;
    }
}
