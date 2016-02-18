package unidue.rc.dao;

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

import unidue.rc.model.Entry;
import unidue.rc.model.Headline;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 08.01.14 09:03
 */
public class HeadlineDAOImpl extends EntryDAOImpl implements HeadlineDAO {

    @Override
    public void move(Entry entry, Headline headline) throws CommitException {

        Headline assignedHeadline = entry.getAssignedHeadline();
        if (assignedHeadline != null && assignedHeadline.equals(headline))
            return;

        Headline nextHeadline = getNextHeadline(headline);
        /*
        two possibilities:
        1st: there is no following headline -> move entry to the end
        2nd: there is a following headline -> move next headline and all following entries one position up
         */
        if (nextHeadline == null) {
            entry.setPosition(entry.getReserveCollection().getMaxEntryIndex() + 1);
        } else {
            // save position so a filter for following entries can be performed
            int newPosition = nextHeadline.getEntry().getPosition();

            // get all entries following the next headline inclusive
            List<Entry> successors = entry.getReserveCollection().getEntries().stream()
                    .filter(e -> e.getPosition() >= nextHeadline.getEntry().getPosition())
                    .collect(Collectors.toList());

            // move all entries one position down
            for (Entry successor : successors) {
                successor.moveDown();
                update(successor);
            }

            // finally set new position of target entry
            entry.setPosition(newPosition);
        }
        update(entry);
    }

    @Override
    public Headline getNextHeadline(Headline origin) {

        List<Headline> headlines = origin.getEntry().getReserveCollection().getHeadlines();
        Optional<Headline> nextHeadline = headlines.stream()
                .skip(headlines.indexOf(origin) + 1)
                .findFirst();
        return nextHeadline.isPresent() ? nextHeadline.get() : null;
    }

    public boolean isEntryAssignedToHeadline(Entry entry, Headline headline) {
        Optional<Headline> entryHeadline = entry.getReserveCollection().getEntries()
                .stream()
                .filter(e -> e.getPosition() < entry.getPosition())
                .filter(e -> e.getHeadline() != null)
                .sorted(Comparator.<Entry>reverseOrder())
                .map(e -> e.getHeadline())
                .findFirst();
        return entryHeadline.isPresent() && entryHeadline.get().equals(headline);
    }
}
