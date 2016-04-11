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
package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import org.apache.cayenne.Persistent;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.tapestry5.beaneditor.DataType;
import unidue.rc.model.auto._ReserveCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReserveCollection extends _ReserveCollection implements IntPrimaryKey, CollectionVisitable {

    private static final long serialVersionUID = 1L;

    @JsonGetter
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }

    @DataType("text")
    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @DataType("text")
    @Override
    public String getReadKey() {
        return super.getReadKey();
    }

    @DataType("text")
    @Override
    public String getWriteKey() {
        return super.getWriteKey();
    }

    @Override
    public List<Entry> getEntries() {

        return super.getEntries()
                .stream()
                .filter(entry -> !entry.isDeleted())
                .sorted(Entry.POSITION_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<Headline> getHeadlines() {
        return getEntries()
                .stream()
                .filter(entry -> entry.getHeadline() != null)
                .map(entry -> entry.getHeadline())
                .collect(Collectors.toList());
    }

    public List<Book> getBooksWithSignature() {
        return getEntries()
                .stream()
                .filter(entry -> entry.getBook() != null)
                .map(entry -> entry.getBook())
                .filter(book -> book.getSignature() != null)
                .collect(Collectors.toList());
    }

    public List<Resource> getResources() {
        List<Resource> result = new ArrayList<>();
        for (Entry entry : getEntries()) {
            Persistent entryValue = entry.getValue();
            if (entryValue instanceof ResourceContainer) {
                ResourceContainer container = (ResourceContainer) entryValue;
                if (container.getResource() != null)
                    result.add(container.getResource());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("values", values)
                .append("objectId", objectId)
                .append("persistenceState", persistenceState)
                .toString();
    }


    public int getMaxEntryIndex() {
        List<Entry> entries = getEntries();
        int maxIndex = 0;
        for (Entry entry : entries)
            if (entry.getPosition() != null)
                maxIndex = Math.max(maxIndex, entry.getPosition());
        return maxIndex;
    }

    public List<Participation> getDocentParticipations() {
        return getParticipations().stream()
                .filter(participation -> DefaultRole.DOCENT.getName().equals(participation.getRole().getName()))
                .collect(Collectors.toList());
    }

    public boolean isRenewable() {
        ReserveCollectionStatus status = getStatus();
        return !ReserveCollectionStatus.NEW.equals(status)
                && !ReserveCollectionStatus.ACTIVE.equals(status);
    }

    public boolean isActivateable() {
        return !ReserveCollectionStatus.ACTIVE.equals(getStatus());
    }

    public boolean isDeactivateable() {
        ReserveCollectionStatus status = getStatus();
        return !ReserveCollectionStatus.DEACTIVATED.equals(status)
                && !ReserveCollectionStatus.ARCHIVED.equals(status);
    }

    public boolean isArchiveable() {
        return !ReserveCollectionStatus.ARCHIVED.equals(getStatus());
    }

    public boolean isActive() {
        return ReserveCollectionStatus.ACTIVE.equals(getStatus());
    }

    public boolean isNew() {
        return ReserveCollectionStatus.NEW.equals(getStatus());
    }

    @Override
    public void accept(CollectionVisitor visitor) {

        visitor.visit(this);

        getLibraryLocation().accept(visitor);

        visitor.startList(ENTRIES_PROPERTY);
        getEntries().forEach(entry -> entry.accept(visitor));
        visitor.endList();

        visitor.startList(PARTICIPATIONS_PROPERTY);
        getParticipations().forEach(participation -> participation.accept(visitor));
        visitor.endList();

        visitor.didVisit(this);
    }
}
