package unidue.rc.ui.components;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.model.BookChapter;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;

/**
 * Created by nils on 31.07.15.
 */
public class BookChapterFormFragment {

    @Parameter
    private Form form;

    @Parameter
    @Property
    private BookChapter chapter;

    @Parameter
    @Property
    private ReserveCollection collection;

    @Property(read = false)
    @Parameter
    private Headline headline;

    @Parameter
    @Property
    private boolean isVisible;

    @Inject
    private Messages messages;

    @Component(id = "chapterauthors")
    private TextField chapterAuthorsField;

    @Component(id = "chaptertitle")
    private TextField chapterTitleField;

    @Component(id = "chaptercomment")
    private TextField commentField;

    @Component(id = "isbn")
    private TextField isbnField;

    @Component(id = "edition")
    private TextField editionField;

    @Component(id = "bookauthors")
    private TextField bookAuthorsField;

    @Component(id = "booktitle")
    private TextField bookTitleField;

    @Component(id = "bookpagesfrom")
    private TextField bookPageStartField;

    @Component(id = "bookpagesto")
    private TextField bookPageEndField;

    @Component(id = "placeofpublication")
    private TextField placeOfPublicationField;

    @Component(id = "publisher")
    private TextField publisherField;

    @Component(id = "signature")
    private TextField signatureField;

    public void validate() {
        // bookAuthors length="2048"
        checkMaxChars(chapter.getBookAuthors(), bookAuthorsField, 2048);

        // bookTitle isMandatory="true" length="1024"
        if (StringUtils.isBlank(chapter.getBookTitle()))
            form.recordError(bookTitleField, messages.get("booktitle-required-message"));
        checkMaxChars(chapter.getBookTitle(), bookTitleField, 1024);

        // chapterAuthors length="2048"
        checkMaxChars(chapter.getChapterAuthors(), chapterAuthorsField, 2048);

        // chapterTitle isMandatory="true" length="1024"
        if (StringUtils.isBlank(chapter.getChapterTitle()))
            form.recordError(chapterTitleField, messages.get("chaptertitle-required-message"));
        checkMaxChars(chapter.getChapterTitle(), chapterTitleField, 2048);

        // comment length="2048"
        checkMaxChars(chapter.getComment(), commentField, 2048);

        // edition length="512"
        checkMaxChars(chapter.getEdition(), editionField, 512);

        // isbn length="24"
        checkMaxChars(chapter.getIsbn(), isbnField, 24);

        // pageEnd isMandatory="true" length="16"
        if (StringUtils.isBlank(chapter.getPageEnd()))
            form.recordError(bookPageEndField, messages.get("bookPagesTo-required-message"));
        checkMaxChars(chapter.getPageEnd(), bookPageEndField, 16);

        // pageStart isMandatory="true" length="16"
        if (StringUtils.isBlank(chapter.getPageStart()))
            form.recordError(bookPageStartField, messages.get("bookPagesFrom-required-message"));
        checkMaxChars(chapter.getPageStart(), bookPageStartField, 16);

        // placeOfPublication length="512"
        checkMaxChars(chapter.getPlaceOfPublication(), placeOfPublicationField, 512);

        // publisher length="512"
        checkMaxChars(chapter.getPublisher(), publisherField, 512);

        // signature length="64"
        checkMaxChars(chapter.getSignature(), signatureField, 64);

    }

    private void checkMaxChars(String value, Field field, int count) {
        if (StringUtils.length(value) > count)
            form.recordError(field, getMaxCharsErrorMessage(count));
    }

    private String getMaxCharsErrorMessage(int count) {
        return messages.format("default-max-chars-message", count);
    }

    public Headline getHeadline() {
        return headline;
    }
}
