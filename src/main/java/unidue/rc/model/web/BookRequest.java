package unidue.rc.model.web;


import unidue.rc.model.OpacFacadeBook;
import unidue.rc.model.OpacFacadeLibraryDataItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by nils on 12.10.15.
 */
public class BookRequest {

    private String searchWord;

    private List<String> breadcrumbs;

    private List<OpacFacadeBook> availableBooks;

    private OpacFacadeBook chosenBook;

    private List<OpacFacadeLibraryDataItem> libraryItems;

    private OpacFacadeLibraryDataItem chosenItem;

    public BookRequest() {
        availableBooks = new ArrayList<>();
        libraryItems = new ArrayList<>();
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public List<String> getBreadcrumbs() {
        return breadcrumbs;
    }

    public void setBreadcrumbs(List<String> breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }

    public void addBreadcrumb(String value) {
        if (breadcrumbs == null)
            this.breadcrumbs = new ArrayList<>();

        breadcrumbs.add(value);
    }

    public List<OpacFacadeBook> getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(List<OpacFacadeBook> availableBooks) {
        this.availableBooks = availableBooks;
    }

    public OpacFacadeBook getChosenBook() {
        return chosenBook;
    }

    public void setChosenBook(OpacFacadeBook chosenBook) {
        this.chosenBook = chosenBook;
    }

    public List<OpacFacadeLibraryDataItem> getLibraryItems() {
        return libraryItems;
    }

    public void setLibraryItems(List<OpacFacadeLibraryDataItem> libraryItems) {
        this.libraryItems = libraryItems;
    }

    public OpacFacadeLibraryDataItem getChosenItem() {
        return chosenItem;
    }

    public void setChosenItem(OpacFacadeLibraryDataItem chosenItem) {
        this.chosenItem = chosenItem;
    }

    public void reduceItemsByLocation() {

        if (libraryItems == null)
            return;

        List<OpacFacadeLibraryDataItem> filtered = new ArrayList<>();
        for (OpacFacadeLibraryDataItem libraryItem : libraryItems) {
            Optional<OpacFacadeLibraryDataItem> presentItem = filtered.stream()
                    .filter(i -> i.getLocation().equals(libraryItem.getLocation()))
                    .findAny();
            if (!presentItem.isPresent())
                filtered.add(libraryItem);
        }
        libraryItems = filtered;
    }

    public boolean containsLibraryItems() {
        return libraryItems != null && libraryItems.size() > 0;
    }

    public boolean containsMultipleLibraryItems() {
        return libraryItems != null && libraryItems.size() > 1;
    }
}
