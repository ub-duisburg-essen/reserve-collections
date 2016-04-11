package unidue.rc.model.web;


import org.apache.tapestry5.upload.services.UploadedFile;
import unidue.rc.model.BookChapter;
import unidue.rc.model.OpacFacadeBook;

import java.util.ArrayList;
import java.util.List;

/**
 * StorageClass to cache information provided by the bookchapter wizard
 */
public class BookChapterWizardData {

    private BookChapter chapter = new BookChapter();

    /* A list of uploaded Files
    * here only index 0 is used
    * */
    private List<UploadedFile> uploads;

    private ArrayList<String> breadcrumbs = new ArrayList<String>();

    public ArrayList<String> getBreadcrumbs() {
        return breadcrumbs;
    }


    /*
    * URL String with protocol
    * */
    private String url;

    /*
   * Reference-Number for Scanjob / Bookjob
   * */
    private String refNo;

    /*
   * List of books which are provided via booksearch
   * */
    private List<OpacFacadeBook> books;

    /*
    * Book which supplies information about the source of the bookchapter
    * */
    private OpacFacadeBook book;

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName() + ": ");
        return buf.toString();
    }

    public List<UploadedFile> getUploads() {
        return uploads;
    }

    public void setUploads(List<UploadedFile> uploads) {
        this.uploads = uploads;
    }

    public List<OpacFacadeBook> getBooks() {
        return books;

    }

    public void setBooks(List<OpacFacadeBook> books) {
        this.books = books;

    }

    public OpacFacadeBook getBookToAdd() {
        return book;

    }

    public void setBookToAdd(OpacFacadeBook bookToAdd) {
        this.book = bookToAdd;

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public BookChapter getChapter() {
        return chapter;
    }
}
