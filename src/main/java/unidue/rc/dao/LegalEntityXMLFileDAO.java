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

import miless.model.LegalEntity;
import org.apache.cayenne.di.Inject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.system.SystemConfigurationService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A <code>LegalEntityXMLFileDAO</code> is able to load and update {@linkplain LegalEntity} objects of miless
 * file backend. Miless keeps all legal entities inside a directory structure configured in mycore.properties by a
 * specific structure. If for example 4-2-2 is configured the base directory contains subdirs ####/## where files are
 * stored with the complete id. </p>
 * <pre>
 *     /miless/basedir/legalentities/0001/23/legalentity_00012345.xml
 * </pre>
 * The first slot of 4 numbers is filled up with zeros, the second is as is, the third slot is within the filename
 * itself and contains the complete id including the last 2 numbers.
 *
 * @author Nils Verheyen
 */
public class LegalEntityXMLFileDAO implements LegalEntityDAO {

    private static final Logger LOG = LoggerFactory.getLogger(LegalEntityXMLFileDAO.class);

    /**
     * Base directory to read xml files from.
     */
    private final File baseDir;

    /**
     * Contains the slot layout in ordner mentioned in class description.
     */
    private List<Integer> directoryLayout;

    /**
     * Sum of all numbers in {@linkplain #directoryLayout}
     */
    private int directoryLength;

    public LegalEntityXMLFileDAO(@Inject SystemConfigurationService sysConfig) {
        this.baseDir = new File(sysConfig.getString("legal.entity.basedir"));
        this.directoryLayout = new ArrayList<>();
        directoryLayout.add(4);
        directoryLayout.add(2);
        directoryLayout.add(2);
        directoryLength = 8;
    }

    public LegalEntityXMLFileDAO(File baseDir, Integer... directoryLayout) {
        this.baseDir = baseDir;
        this.directoryLayout = new ArrayList<>();

        for (Integer dir : directoryLayout) {
            this.directoryLayout.add(dir);
            directoryLength += dir;
        }
    }

    public LegalEntity getLegalEntityById(Integer id) {

        // build path
        File legalEntityFile = getLegalEntityFile(id);

        return legalEntityFile != null ? read(legalEntityFile) : null;
    }

    /**
     * Builds the path described in {@linkplain LegalEntityXMLFileDAO} and returns the {@linkplain File} if it exists.
     */
    private File getLegalEntityFile(Integer id) {

        // directory file structure example: ####/##/legalentity_########.xml

        // build leid in form "00012345"
        StringBuilder leID = new StringBuilder(id.toString());
        while (leID.length() < directoryLength) {
            leID.insert(0, 0);
        }

        // build subdirectory ####/##
        File legalEntityDirectory = new File(baseDir.getAbsolutePath());
        int currentSubdirLength = 0;
        for (int i = 0; i < directoryLayout.size() - 1; i++) {
            Integer dirLength = directoryLayout.get(i);
            String subdir = leID.substring(currentSubdirLength, dirLength + currentSubdirLength).toString();
            legalEntityDirectory = new File(legalEntityDirectory, subdir);
            currentSubdirLength += dirLength;
        }

        // build filename legalentity_########.xml
        StringBuilder legalEntityFilename = new StringBuilder("legalentity_");
        legalEntityFilename.append(leID);
        legalEntityFilename.append(".xml");

        File legalEntityFile = new File(legalEntityDirectory, legalEntityFilename.toString());
        LOG.debug("returning legal entity file from " + legalEntityFile.getAbsolutePath());
        return legalEntityFile.exists() ? legalEntityFile : null;
    }

    /**
     * Tries to read a {@linkplain LegalEntity} object of target {@linkplain File}.
     *
     * @return the parsed {@linkplain LegalEntity} if it could be read, <code>null</code> otherwise
     */
    private LegalEntity read(File file) {
        // read xml file
        Serializer s = new Persister(new AnnotationStrategy());
        LegalEntity result = null;
        try {
            result = s.read(LegalEntity.class, file);
        } catch (Exception e) {
            LOG.error("could not read legal entity from file " + file.getAbsolutePath() + " " + e.getMessage());
            if (LOG.isDebugEnabled())
                e.printStackTrace();
        }
        return result;
    }
}
