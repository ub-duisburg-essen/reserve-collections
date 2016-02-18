package unidue.rc.migration.filefilter;

/*
 * #%L
 * Semesterapparate
 * $Id$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

/**
 * The ReserveFilter returns true if a given File has a special suffix.
 * 
 * @author Marcus Koesters

 */

import java.io.File;
import java.io.FileFilter;

public class ReserveCollectionFileFilter implements FileFilter {

    private static String COLLECTION_FILE_SUFFIX = ".msa";

    private String documentID;

    public ReserveCollectionFileFilter() {
    }

    public ReserveCollectionFileFilter(String documentID) {
        this.documentID = documentID;
    }

    @Override
    public boolean accept(File pathname) {
        String filename = pathname.getName();
        int startIndexDocID = filename.lastIndexOf('-');
        int endIndexDocID = filename.lastIndexOf('_');
        return documentID != null // document id must be present
                && startIndexDocID >= 0 // start index of document id
                && endIndexDocID > startIndexDocID // end index of document > start index
                && filename.substring(startIndexDocID + 1, endIndexDocID).equals(documentID) // doc id present?
                && filename.endsWith(COLLECTION_FILE_SUFFIX); // ends with .msa
    }

}
