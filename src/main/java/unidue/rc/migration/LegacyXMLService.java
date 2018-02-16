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
package unidue.rc.migration;


import org.apache.commons.configuration2.ex.ConfigurationException;
import unidue.rc.model.legacy.Document;

import java.io.File;
import java.util.Collection;

/**
 * Created by nils on 13.07.15.
 */
public interface LegacyXMLService {

    /**
     * Sets a <code>movedPermanent</code> attribute to the root element of a legacy collection xml file, that contains
     * the url where the collection has moved to.
     *
     * @param legacyCollectionXML file pointing to the legacy collection
     * @param movedURL            permanent url where the collection has moved to
     * @throws MigrationException thrown if the url could not be set
     */
    void setMoved(File legacyCollectionXML, String movedURL) throws MigrationException;

    /**
     * Sets a <code>migrationCode</code> attribute to the root element of a legacy collection xml file, that contains
     * the code with which a collection can be migrated without authentication.
     *
     * @param legacyCollectionXML file pointing to the legacy collection
     * @param code                code that must be used for migration
     * @throws MigrationException thrown if the code could not be set
     */
    void setMigrationCode(File legacyCollectionXML, String code) throws MigrationException;

    /**
     * Returns the migration code of the root element of target xml file.
     *
     * @param legacyCollectionXML file pointing to the legacy collection
     * @return the migration code
     * @throws MigrationException thrown if something went wrong during xml deserialization
     */
    String getMigrationCode(File legacyCollectionXML) throws MigrationException;

    /**
     * Returns the derivate id that the file with target name belongs to.
     *
     * @param filename filename of the derivate
     * @return der derivate id if one could be found, an empty string if not
     */
    String getDerivateID(String filename);

    /**
     * Returns the document id that the file with target name belongs to.
     *
     * @param filename filename of the document
     * @return der document id if one could be found, an empty string if not
     */
    String getDocumentID(String filename);

    /**
     * Builds a relative file path with base configured in <code>legacy.document.metadata.path</code> to the
     * document metadata.
     *
     * @param docID document id to use for the path
     * @return filepath of the document
     */
    String buildMetaDataFilePath(String docID);

    /**
     * Normalizes target isbn that it contains only one isbn and the first numbers are stripped of (<code>[^0-9;-]</code>).
     *
     * @param isbn isbn to normalize
     * @return normalized isbn
     */
    String normalizeISBN(String isbn);

    /**
     * Returns a collection of all reserve collections that are used as durable collections.
     *
     * @return collection of all documents
     * @throws ConfigurationException thrown on mis configured semester ends
     */
    Collection<Document> getDurableCollectionDocIDs() throws ConfigurationException;
}
