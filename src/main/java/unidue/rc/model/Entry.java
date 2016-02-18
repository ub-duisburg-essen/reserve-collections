package unidue.rc.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.cayenne.Persistent;
import unidue.rc.model.auto._Entry;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 * <p>An <code>Entry</code> is a relation between a {@link ReserveCollection} and a concrete {@link EntryValue} that
 * binds the two objects together.</p>
 *
 * @see JournalArticle
 * @see BookChapter
 * @see Book
 * @see Reference
 * @see File
 * @see Headline
 * @see Html
 * @see WebLink
 */
public class Entry extends _Entry implements Comparable<Entry>, IntPrimaryKey, CollectionVisitable {

    public static final Comparator<? super Entry> POSITION_COMPARATOR = (o1, o2) -> {
        Integer o1Position = o1.getPosition();
        Integer o2Position = o2.getPosition();
        return o1Position != null && o2Position != null
                ? o1Position.compareTo(o2Position)
                : 0;
    };

    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }

    @Override
    public int compareTo(Entry o) {
        Integer rcIndex = getPosition();
        Integer other = o != null ? o.getPosition() : null;
        return rcIndex != null && other != null // compare against each other if both of them are not null
                ? rcIndex.compareTo(other)
                : rcIndex != null && o == null //
                        ? -1
                        : rcIndex == null && o != null
                                ? 1
                                : 0;
    }

    public void moveDown() {
        setPosition(getPosition() + 1);
    }

    public boolean isDeleted() {
        return getDeleted() != null;
    }

    public void setDeleted(boolean deleted) {
        Date newValue = deleted ? new Date() : null;
        setDeleted(newValue);
    }

    public Headline getAssignedHeadline() {

        Optional<Headline> optional = getReserveCollection().getEntries()
                .stream()
                .filter(e -> e.getPosition() < this.getPosition())
                .filter(e -> e.getHeadline() != null)
                .map(e -> e.getHeadline())
                .sorted(Comparator.<Headline>reverseOrder())
                .findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    public Persistent getValue() {
        if (getHeadline() != null)
            return getHeadline();
        else if (getBook() != null)
            return getBook();
        else if (getBookChapter() != null)
            return getBookChapter();
        else if (getFile() != null)
            return getFile();
        else if (getJournalArticle() != null)
            return getJournalArticle();
        else if (getHtml() != null)
            return getHtml();
        else if (getWebLink() != null)
            return getWebLink();
        else if (getReference() != null)
            return getReference();
        return null;
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);

        Persistent value = getValue();
        if (value instanceof CollectionVisitable)
            ((CollectionVisitable) value).accept(visitor);

        visitor.didVisit(this);
    }
}
