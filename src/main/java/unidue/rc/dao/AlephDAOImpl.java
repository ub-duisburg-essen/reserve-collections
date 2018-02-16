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
package unidue.rc.dao;


import org.apache.cayenne.di.Inject;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import unidue.rc.model.Book;
import unidue.rc.plugins.alephsync.AlephBook;
import unidue.rc.system.BookUtils;
import unidue.rc.system.SystemConfigurationService;

import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

/**
 * Created by nils on 29.06.15.
 */
public class AlephDAOImpl implements AlephDAO {

    private static final Logger LOGGER = Logger.getLogger(AlephDAOImpl.class);

    private static final Charset UTF_CHARSET = Charset.forName("UTF-8");

    private static final String[] MAB_FIELD_TITLE = { "331|a", "331a|a", "331b|a" };
    private static final String[] MAB_FIELD_AUTHOR = { "359|a" };
    private static final String[] MAB_FIELD_VOLUME = { "089|a" };
    private static final String[] MAB_FIELD_EDITION = { "403|a" };
    private static final String[] MAB_FIELD_PLACE = { "410|a" };
    private static final String[] MAB_FIELD_PUBLISHER = { "412|a" };
    private static final String[] MAB_FIELD_YEAR = { "425a|a" };
    private static final String[] MAB_FIELD_ISBN = { "540a|a", "540b|a", "540|a" };
    private static final String[] MAB_FIELD_SUPERORDINATION = { "010|a" };

    @Inject
    private SystemConfigurationService config;

    private Connection connection;

    public AlephDAOImpl(@Inject SystemConfigurationService config) throws ConfigurationException {

        this.config = config;

        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception ex) {
            String msg = "Error while registering Oracle JDBC driver";
            throw new ConfigurationException(msg);
        }
    }

    @Override
    public List<AlephBook> listSignatures(String systemID) throws SQLException {

        open();

        String sql = "select z30_call_no, z30_rec_key " +
                " from edu50.z30" +
                " left join edu50.z36 on z30_rec_key = z36_rec_key" +
                " where z36_id = '" + systemID + "'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<AlephBook> books = new ArrayList<>();
        while (rs.next()) {
            String signature = rs.getString(1);
            if (rs.wasNull()) {
                LOGGER.info("WARNING: entry without signature for aleph system ID " + systemID);
                continue;
            }

            signature = BookUtils.getNormalized(signature);
            String key = rs.getString(2).substring(0, 9);
            AlephBook book = new AlephBook();
            book.setSignature(signature);
            book.setRecordID(key);
            books.add(book);
        }

        st.close();
        rs.close();

        close();
        return books;
    }

    public void setBookData(Book bookToUpdate, String signature) throws SQLException {

        open();

        String key = getRecordID(signature);

        String sys = (key == null ? "000508819" : getSysKeyForSrcKey(key));
        Properties data = getProperties(sys);

        Properties uodata = data;
        String uoht = getProperty(data, MAB_FIELD_SUPERORDINATION);
        if (uoht != null) {
            sys = getSysKeyForHT(uoht);
            if (sys != null)
                uodata = getProperties(sys);
        }

        String title = getProperty(uodata, MAB_FIELD_TITLE);
        bookToUpdate.setTitle(title);

        String author = getProperty(uodata, MAB_FIELD_AUTHOR);
        bookToUpdate.setAuthors(author);

        String volume = getProperty(data, MAB_FIELD_VOLUME);
        if (volume != null) {
            String t = getProperty(data, MAB_FIELD_TITLE);
            String a = getProperty(data, MAB_FIELD_AUTHOR);

            if ((t != null) || (a != null))
                volume += ". ";

            if (t != null) {
                volume += t;
                if (a != null)
                    volume += " / ";
            }

            if (a != null)
                volume += a;
        }
        bookToUpdate.setVolume(volume);

        String edition = getProperty(data, MAB_FIELD_EDITION);
        if (edition == null)
            edition = getProperty(uodata, MAB_FIELD_EDITION);
        bookToUpdate.setEdition(edition);

        String place = getProperty(uodata, MAB_FIELD_PLACE);
        bookToUpdate.setPlaceOfPublication(place);

        String publisher = getProperty(uodata, MAB_FIELD_PUBLISHER);
        bookToUpdate.setPublisher(publisher);

        String year = getProperty(data, MAB_FIELD_YEAR);
        if (!StringUtils.isEmpty(year) && year.matches("[0-9]+"))
            bookToUpdate.setYearOfPublication(Integer.valueOf(year));

        String isbn = getProperty(data, MAB_FIELD_ISBN);
        if (isbn == null)
            isbn = getProperty(uodata, MAB_FIELD_ISBN);
        bookToUpdate.setIsbn(isbn);

        bookToUpdate.setSignature(BookUtils.getNormalized(signature));

        close();
    }

    /** Gets MAB metadata for an entry in the aleph database **/
    private Properties getProperties(String z00_sys_key) throws SQLException {

        String sql = "select z00_data, z00_data_len from edu01.z00 where z00_doc_number = ?";
        PreparedStatement st_z00_data = connection.prepareStatement(sql);

        Properties properties = new Properties();

        st_z00_data.setString(1, z00_sys_key);
        ResultSet rs = st_z00_data.executeQuery();

        if (rs.next()) {
            byte[] clob = rs.getBytes(1);
            int dlen = rs.getInt(2);

            LOGGER.debug("---------------------------------------");
            LOGGER.debug(StringUtils.toEncodedString(clob, UTF_CHARSET));
            LOGGER.debug("---------------------------------------");

            int offset = 0;
            while (offset < dlen) {
                byte[] tmp = new byte[4];
                System.arraycopy(clob, offset, tmp, 0, 4);
                String slen = new String(tmp, UTF_CHARSET);
                int len = Integer.parseInt(slen);
                offset += 4;

                LOGGER.debug("field len  = " + len);

                tmp = new byte[len];
                System.arraycopy(clob, offset, tmp, 0, len);
                String field = new String(tmp, UTF_CHARSET);
                offset += len;

                String fieldname = field.substring(0, 5).trim();
                String fieldcont = field.substring(6).trim();

                LOGGER.debug("field name = " + fieldname);
                LOGGER.debug("field cont = " + fieldcont);

                if (!fieldcont.startsWith("$$")) {
                    if (fieldcont.length() > 0)
                        putProperty(properties, fieldname, fieldcont);
                } else {
                    int dd1 = 0, dd2 = 0;

                    while (dd2 < fieldcont.length()) {
                        dd1 = fieldcont.indexOf("$$", dd2);
                        String ufk = fieldcont.substring(dd1 + 2, dd1 + 3);

                        dd2 = fieldcont.indexOf("$$", dd1 + 3);
                        if (dd2 < 0)
                            dd2 = fieldcont.length();

                        String cont = fieldcont.substring(dd1 + 3, dd2).trim();
                        putProperty(properties, fieldname + "|" + ufk, cont);
                    }
                }
            }
        }
        rs.close();
        st_z00_data.close();

        return properties;
    }

    private void putProperty(Properties p, String key, String value) {
        if (p.containsKey(key))
            value = p.getProperty(key) + ", " + value;

        LOGGER.debug("set property " + key + " = " + value);
        p.setProperty(key, value);
    }

    private String getProperty(Properties data, String[] fields) {
        Optional<String> property = Arrays.stream(fields)
                .filter(field -> data.containsKey(field))
                .map(field -> data.getProperty(field))
                .findAny();
        return property.isPresent()
                ? property.get()
                : null;
    }

    private String getSysKeyForHT(String ht) throws SQLException {
        String sql = "select z11_doc_number" +
                " from edu01.z11" +
                " where z11_text = '$$a" + ht + "'";
        return getKeyBySQL(sql);
    }

    private String getSysKeyForSrcKey(String src_key) throws SQLException {
        String sql = "select substr(z103_rec_key_1, 6, 9)" +
                " from edu50.z103" +
                " where z103_lkr_library = 'EDU50'" +
                " and z103_rec_key like 'EDU50" + src_key + "%'";
        return getKeyBySQL(sql);
    }

    private String getRecordID(String signature) throws SQLException {
        String sql = "select substr(z30_rec_key, 0, 9)" +
                " from edu50.z30" +
                " where z30_call_no = '" + signature + "'";
        return getKeyBySQL(sql);
    }

    private String getKeyBySQL(String sql) throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        String key = null;
        if (rs.next())
            key = rs.getString(1);

        rs.close();
        st.close();

        return key;
    }

    private void open() throws SQLException {

        String prefix = "aleph.oracle.";
        String userID = config.getString(prefix + "userID");
        String passwd = config.getString(prefix + "passwd");
        String dburi = config.getString(prefix + "databaseURI");

        LOGGER.info("Connecting to " + dburi + "...");
        connection = DriverManager.getConnection(dburi, userID, passwd);
        LOGGER.info("Connected to Aleph.");
    }

    private void close() throws SQLException {
        connection.close();
    }
}
