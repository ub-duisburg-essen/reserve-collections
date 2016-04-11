package unidue.rc.migration;


import org.apache.cayenne.di.Inject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Setting;
import unidue.rc.model.legacy.Slots;
import unidue.rc.system.SystemConfigurationService;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Created by nils on 13.07.15.
 */
public class LegacyXMLServiceImpl implements LegacyXMLService {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyXMLServiceImpl.class);

    private static final String MIGRATION_CODE_ATTRIBUTE = "migrationCode";
    private static final String MOVED_PERMANENT_ATTRIBUTE = "movedPermanent";
    private static final DateFormat SLOT_VALID_TO_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private static final Format DEFAULT_FORMAT = Format
            .getPrettyFormat()
            .setOmitDeclaration(false)
            .setOmitEncoding(false);

    @Inject
    private SystemConfigurationService config;

    @Override
    public void setMoved(File legacyCollectionXML, String movedURL) throws MigrationException {

        Document jdomDocument = readDocument(legacyCollectionXML);

        Element rootElement = jdomDocument.getRootElement();
        rootElement.setAttribute(MOVED_PERMANENT_ATTRIBUTE, movedURL);

        update(rootElement, legacyCollectionXML);
        LOG.info("set moved permanent to derivate " + getDerivateID(legacyCollectionXML.getAbsolutePath()));
    }

    @Override
    public void setMigrationCode(File legacyCollectionXML, String code) throws MigrationException {
        Document jdomDocument = readDocument(legacyCollectionXML);

        Element rootElement = jdomDocument.getRootElement();
        rootElement.setAttribute(MIGRATION_CODE_ATTRIBUTE, code);

        update(rootElement, legacyCollectionXML);
        LOG.info("added migration code to derivate " + getDerivateID(legacyCollectionXML.getAbsolutePath()));
    }

    @Override
    public String getMigrationCode(File legacyCollectionXML) throws MigrationException {
        Document jdomDocument = readDocument(legacyCollectionXML);

        Element rootElement = jdomDocument.getRootElement();
        return rootElement.getAttributeValue(MIGRATION_CODE_ATTRIBUTE);
    }

    @Override
    public String getDerivateID(String filename) {
        if (filename != null) {
            String[] s = filename.split("_");
            String derivateID = s[s.length - 1];
            derivateID = derivateID.substring(0, derivateID.lastIndexOf('.'));
            return derivateID;
        }
        return "";
    }

    @Override
    public String getDocumentID(String filename) {
        if (filename != null) {
            int start = filename.lastIndexOf('-') + 1;
            int end = filename.lastIndexOf('_');

            String documentID = filename.substring(start, end);
            return documentID;
        }
        return "";
    }

    @Override
    public String buildMetaDataFilePath(String docID) {
        StringBuilder newDocID = new StringBuilder(addLeadingZeros(docID, 8));
        String filename = "miless_mods_" + newDocID + ".xml";
        int calc = Integer.valueOf(newDocID.toString());
        String firstlevel = String.valueOf((calc / 10000));
        firstlevel = addLeadingZeros(firstlevel, 4);
        String secondlevel = String.valueOf(Integer.valueOf(newDocID.delete(0, 4).toString()) / 100);
        secondlevel = addLeadingZeros(secondlevel, 2);
        String path = firstlevel + File.separatorChar + secondlevel + File.separatorChar + filename;
        return path;
    }

    @Override
    public String normalizeISBN(String isbn) {
        if (isbn != null) {
            if (isbn.contains(",")) {
                String[] newISBN = isbn.split(",");
                isbn = newISBN[0].replaceAll("[^0-9;-]", "");
            }
            if (isbn.contains(";")) {
                String[] newISBN = isbn.split(";");
                isbn = newISBN[0].replaceAll("[^0-9;-]", "");
            }


        }
        if ("".equals(isbn)) {
            return null;
        }

        return isbn;
    }

    @Override
    public Collection<unidue.rc.model.legacy.Document> getDurableCollectionDocIDs() throws ConfigurationException {
        File slotXml = new File(config.getString("legacy.slot.list.xml.file"));
        Serializer serializer = new Persister();
        Slots slots;
        try {
            slots = serializer.read(Slots.class, slotXml);
        } catch (Exception e) {
            LOG.error("could not read slots from " + slotXml.getAbsolutePath(), e);
            return null;
        }
        Setting noSemesterEndSetting = config.getSetting("no.semester.end");
        SimpleDateFormat dateFormat = new SimpleDateFormat(noSemesterEndSetting.getFormat());
        Date noSemesterEnd;
        try {
            noSemesterEnd = dateFormat.parse(noSemesterEndSetting.getValue());
        } catch (ParseException e) {
            throw new ConfigurationException("invalid no.semester.end format", e);
        }
        return slots.getSlots().stream()
                .filter(slot -> slot.getValidTo() != null)
                .filter(slot -> {
                    try {
                        Date validTo = SLOT_VALID_TO_DATE_FORMAT.parse(slot.getValidTo());
                        return validTo.equals(noSemesterEnd) || validTo.after(noSemesterEnd);
                    } catch (ParseException e) {
                        LOG.error("invalid valid to date in slot " + slot.getId());
                    }
                    return false;
                })
                .map(slot -> slot.getDocument())
                .collect(Collectors.toList());
    }

    private void update(Element data, File xml) throws MigrationException {

        File backup = null;
        try {
            // create backup
            backup = backup(xml);

            // set attribute
            write(data, xml);
        } catch (MigrationException e) {
            // rollback
            if (backup != null)
                rollback(backup, xml);
        }
    }

    private String addLeadingZeros(String docID, int totalLength) {
        StringBuilder builder = new StringBuilder(docID);
        while (builder.toString().length() < totalLength) {
            builder.insert(0, "0");
        }

        return builder.toString();
    }

    private File backup(File sourceXML) throws MigrationException {
        String derivateID = getDerivateID(sourceXML.getAbsolutePath());
        try {
            File backupDir = new File(config.getString("migration.backup.dir"), derivateID);
            FileUtils.copyFileToDirectory(sourceXML, backupDir);
            return FileUtils.getFile(backupDir, FilenameUtils.getName(sourceXML.getAbsolutePath()));
        } catch (IOException e) {
            throw new MigrationException("could not create backup", e);
        }
    }

    private void rollback(File backup, File original) throws MigrationException {
        try {
            FileUtils.copyFile(backup, original);
        } catch (IOException e) {
            throw new MigrationException("could not rollback backup " + backup.getAbsolutePath() + " to " + original.getAbsolutePath(), e);
        }
    }

    private void write(Element rootElement, File sourceXML) throws MigrationException {

        Element clone = rootElement.clone();
        Document document = new Document(clone);

        String sourceXMLPath = sourceXML.getAbsolutePath();
        String encoding = getEncoding(sourceXML);

        // create temp file to write xml to
        File tempFile;
        try {
            tempFile = File.createTempFile(FilenameUtils.getName(sourceXMLPath), FilenameUtils.getExtension(sourceXMLPath));
        } catch (IOException e) {
            throw new MigrationException("could not create temp file for source xml " + sourceXMLPath);
        }

        try (OutputStream output = new FileOutputStream(tempFile)) {
            Format format = DEFAULT_FORMAT.clone();
            if (encoding != null)
                format.setEncoding(encoding);

            XMLOutputter xmlOutputter = new XMLOutputter(format);
            xmlOutputter.output(document, output);
        } catch (IOException e) {
            throw new MigrationException("could not write new collection to " + tempFile.getAbsolutePath(), e);
        }
        try {
            FileUtils.copyFile(tempFile, sourceXML);
        } catch (IOException e) {
            throw new MigrationException(e);
        }
    }

    private Document readDocument(File xml) throws MigrationException {
        // the SAXBuilder is the easiest way to create the JDOM2 objects.
        SAXBuilder jdomBuilder = new SAXBuilder();

        // jdomDocument is the JDOM2 Object
        Document jdomDocument;
        try {
            jdomDocument = jdomBuilder.build(xml);
        } catch (JDOMException e) {
            throw new MigrationException("could not parse " + xml, e);
        } catch (IOException e) {
            throw new MigrationException("could not read " + xml, e);
        }
        return jdomDocument;
    }

    private String getEncoding(File xml) {
        try (XmlStreamReader reader = new XmlStreamReader(xml)) {
            return reader.getEncoding();
        } catch (IOException e) {
            LOG.error("could not detect encoding of file " + xml.getAbsolutePath());
            return null;
        }
    }
}
