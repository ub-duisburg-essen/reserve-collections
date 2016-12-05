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

    public boolean isFileAvailable() {
        String filePath = getFilePath();
        return filePath != null
                && !filePath.isEmpty()
                && getFileDeleted() == null;
    }
}
