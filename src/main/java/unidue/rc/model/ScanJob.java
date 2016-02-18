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
