/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.io;


import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.*;
import unidue.rc.model.rss.Channel;
import unidue.rc.model.rss.Item;
import unidue.rc.system.BaseURLService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nils on 20.10.15.
 */
public class CollectionRSSWriterImpl implements CollectionRSSWriter, CollectionVisitor {

    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.GERMANY);
    private static final String DESCRIPTION_LINE_END = "<br/>" + System.getProperty("line.separator");

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SystemMessageService messages;

    @Inject
    private BaseURLService urlService;

    private Channel rss;

    private Item currentItem;

    private List<User> docents;

    @Override
    public Channel serialize(ReserveCollection collection) {

        rss = new Channel();
        collection.accept(this);
        return rss;
    }

    @Override
    public void visit(ReserveCollection collection) {
        List<Participation> participations = participationDAO.getActiveParticipations(roleDAO.getRole(DefaultRole.DOCENT), collection);
        docents = participations.stream()
                .map(participation -> userDAO.getUserById(participation.getUserId()))
                .filter(user -> user != null)
                .collect(Collectors.toList());

        rss.setTitle(String.format("%s - %d %s", collection.getTitle(),
                collection.getNumber().getNumber(),
                collection.getLibraryLocation().getName()));

        List<String> docentNames = docents.stream().map(d -> d.getRealname()).collect(Collectors.toList());
        String authors = StringUtils.join(docentNames, " / ");
        rss.setDescription(String.format("%s / %s", collection.getTitle(), authors));

        Optional<User> optionalDocent = docents.stream().findFirst();
        if (optionalDocent.isPresent()) {
            User docent = optionalDocent.get();
            rss.setManagingEditor(String.format("%s (%s)", docent.getEmail(), docent.getRealname()));
        }

        rss.setWebMaster(String.format("%s (%s)", config.getString("system.mail"), "SemApp"));
        rss.setLink(urlService.getViewCollectionURL(collection));
        rss.setItems(new ArrayList<>());
    }

    @Override
    public void didVisit(ReserveCollection collection) {
    }

    @Override
    public void visit(Participation participation) {
    }

    @Override
    public void visit(LibraryLocation location) {
    }

    @Override
    public void visit(Entry entry) {
        currentItem = new Item();

        currentItem.setPubDate(TIME_FORMATTER.format(entry.getCreated()));
    }

    @Override
    public void didVisit(Entry entry) {
        if (!entry.isDeleted())
            rss.getItems().add(currentItem);
    }

    @Override
    public void visit(Html html) {
        currentItem.setTitle(messages.get("html"));
        currentItem.setDescription(html.getText());
    }

    @Override
    public void visit(Headline headline) {
        currentItem.setTitle(headline.getText());
    }

    @Override
    public void visit(WebLink webLink) {
        currentItem.setLink(webLink.getUrl());
        currentItem.setTitle(webLink.getName());
    }

    @Override
    public void visit(Reference reference) {
        currentItem.setTitle(reference.getTitle());

        String description = buildDescription(
                buildDescriptionEntry("authors", reference.getAuthors()),
                buildDescriptionEntry("volume", reference.getVolume()),
                buildDescriptionEntry("edition", reference.getEdition()),
                buildDescriptionEntry("place.of.publication", reference.getPlaceOfPublication()),
                buildDescriptionEntry("year.of.publication", StringUtils.defaultString(reference.getYearOfPublicationAsString(), null)),
                buildDescriptionEntry("isbn", reference.getIsbn()),
                buildDescriptionEntry("signature", reference.getSignature()),
                buildDescriptionEntry("collection.reference", reference.getCollectionNumber()),
                buildDescriptionEntry("comment", reference.getComment()));
        currentItem.setDescription(description);

        currentItem.setCategory(messages.get("book"));

        if (!StringUtils.isBlank(reference.getSignature()))
            currentItem.setLink(config.getString("primo.sig.search.url") + reference.getSignature());
    }

    @Override
    public void didVisit(Reference reference) {

    }

    @Override
    public void visit(File file) {
        String title = StringUtils.isNotBlank(file.getDescription())
                ? file.getDescription()
                : file.getResource() != null
                    ? file.getResource().getFileName()
                    : messages.get("file");
        currentItem.setTitle(title);
        currentItem.setDescription(buildDescription(
                buildDescriptionEntry("description", file.getDescription()),
                buildDescriptionEntry("mime.type", file.getResource().getMimeType())
        ));
        currentItem.setCategory(messages.get("own.material"));
    }

    @Override
    public void didVisit(File file) {
    }

    @Override
    public void visit(JournalArticle article) {
        currentItem.setTitle(article.getArticleTitle());

        String description = buildDescription(
                buildDescriptionEntry("authors", article.getAuthors()),
                buildDescriptionEntry("journal.title", article.getJournalTitle()),
                buildDescriptionEntry("place.of.publication", article.getPlaceOfPublication()),
                buildDescriptionEntry("issn", article.getIssn()),
                buildDescriptionEntry("signature", article.getSignature()),
                buildDescriptionEntry("page.start", article.getPageStart()),
                buildDescriptionEntry("page.end", article.getPageEnd()),
                buildDescriptionEntry("comment", article.getComment()));
        currentItem.setDescription(description);

        currentItem.setCategory(messages.get("journal.article"));

        if (!StringUtils.isBlank(article.getSignature()))
            currentItem.setLink(config.getString("primo.sig.search.url") + article.getSignature());
    }

    @Override
    public void didVisit(JournalArticle article) {
    }

    @Override
    public void visit(BookChapter chapter) {
        currentItem.setTitle(chapter.getWorkTitle());

        String description = buildDescription(
                buildDescriptionEntry("authors", chapter.getBookAuthors()),
                buildDescriptionEntry("place.of.publication", chapter.getPlaceOfPublication()),
                buildDescriptionEntry("isbn", chapter.getIsbn()),
                buildDescriptionEntry("signature", chapter.getSignature()),
                buildDescriptionEntry("page.start", chapter.getPageStart()),
                buildDescriptionEntry("page.end", chapter.getPageEnd()),
                buildDescriptionEntry("comment", chapter.getComment()));
        currentItem.setDescription(description);

        currentItem.setCategory(messages.get("book.chapter"));

        if (!StringUtils.isBlank(chapter.getSignature()))
            currentItem.setLink(config.getString("primo.sig.search.url") + chapter.getSignature());
    }

    @Override
    public void didVisit(BookChapter chapter) {

    }

    @Override
    public void visit(Book book) {
        currentItem.setTitle(book.getTitle());

        String description = buildDescription(
                buildDescriptionEntry("authors", book.getAuthors()),
                buildDescriptionEntry("volume", book.getVolume()),
                buildDescriptionEntry("edition", book.getEdition()),
                buildDescriptionEntry("place.of.publication", book.getPlaceOfPublication()),
                buildDescriptionEntry("year.of.publication", StringUtils.defaultString(book.getYearOfPublicationAsString(), null)),
                buildDescriptionEntry("isbn", book.getIsbn()),
                buildDescriptionEntry("signature", book.getSignature()),
                buildDescriptionEntry("collection.reference", book.getCollectionNumber()),
                buildDescriptionEntry("comment", book.getComment()));
        currentItem.setDescription(description);

        currentItem.setCategory(messages.get("book"));

        if (!StringUtils.isBlank(book.getSignature()))
            currentItem.setLink(config.getString("primo.sig.search.url") + book.getSignature());
    }

    @Override
    public void didVisit(Book book) {

    }

    @Override
    public void visit(Resource resource) {
        currentItem.setLink(urlService.getDownloadLink(resource));
    }

    @Override
    public void startList(String fieldName) {
    }

    @Override
    public void endList() {
    }

    private String buildDescription(String... values) {
        List<String> filteredValues = Arrays.stream(values)
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return StringUtils.join(filteredValues, DESCRIPTION_LINE_END);
    }

    private String buildDescriptionEntry(String labelKey, String value) {
        return StringUtils.isBlank(value)
                ? null
                : StringUtils.join(new String[]{messages.get(labelKey), value}, ": ");
    }
}
