package unidue.rc.plugins.moodle.services;

/*
 * #%L
 * Semesterapparate
 * $Id:$
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

import org.apache.cayenne.di.Inject;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import unidue.rc.io.XMLStreamResponse;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.Moodle;
import unidue.rc.plugins.moodle.model.ResourceRequest;
import unidue.rc.system.CryptService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.RequestError;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Date;

/**
 * Created by nils on 15.06.15.
 */
public class MoodleServiceImpl implements MoodleService {

    enum SessionKeys {
        URI, CollectionID, ResourceID, Filename, Username, Firstname, Lastname, Email, AuthType
    }

    private static final Logger LOG = Logger.getLogger(MoodleServiceImpl.class);

    private static final URLCodec URL_CODEC = new URLCodec();

    private static final String PRIVATE_KEY_FILENAME = "semapp.private.key";

    private static final String MOODLE_PUBLIC_KEY_FILENAME = "moodle.public.key";

    /**
     * Algorithm used for encryption and decryption with RSA generated asymmetric keys.
     */
    private static final String RSA_KEY_ALGORITHM_NAME = "RSA/None/PKCS1Padding";

    /**
     * Algorithm used for encryption and decryption with RC$ generated symmetric keys.
     */
    private static final String RC4_ALGORITHM_NAME = "RC4";

    /**
     * file pointing to reserve collections private key.
     */
    private File privateKeyFile;

    /** Time of last read of miless private key. */
    private long lastReadPrivateKey;

    /** {@link Key} which is used to decrypt messages from moodle. */
    private Key privateKey;

    /**
     * file pointing to moodles public key configured in mycore.properties by <code>MIL.Moodle.moodle.public.key</code>.
     */
    private File moodlePublicKeyFile;

    /** {@link Key} which is used to encrypt messages delivered to moodle. */
    private Key moodlePublicKey;

    /** Time of last read of moodles public key. */
    private long lastReadMoodlesPublicKey;

    /** {@link CryptService} which is used for encryption and decryption of text. */
    private CryptService crypter;

    /** {@link CryptService} which is used for encryption and decryption of text. */
    private SystemConfigurationService config;

    /**
     * Cache used to save url requests from moodle. Two requests are executed from moodle and the user. The first
     * request generates a temporary encrypted url, that only moodle is able to read. The second request comes from the
     * user to obtain the file.
     */
    private TempFileRequestCache cache;

    public MoodleServiceImpl(@Inject SystemConfigurationService config, @Inject CryptService cryptService) {

        this.config = config;
        this.crypter = cryptService;

        privateKeyFile = new File(config.getString("keystore"), PRIVATE_KEY_FILENAME);
        moodlePublicKeyFile = new File(config.getString("keystore"), MOODLE_PUBLIC_KEY_FILENAME);

        int clearInterval = config.getInt("temp.url.clear.interval");
        int validTime = config.getInt("temp.url.valid.time");
        cache = new TempFileRequestCache(clearInterval, validTime);

        try {
            readPrivateKey();
        } catch (IOException | GeneralSecurityException e) {
            LOG.error("could not read semapp private key", e);
        }
        try {
            readMoodlesPublicKey();
        } catch (IOException e) {
            LOG.error("could not read moodles public key", e);
        }
    }

    public DecryptedRequestData decryptRequestData(HttpServletRequest request) throws RequestError {

        String requestData = request.getParameter("requestData");
        String encodedSymKey = request.getParameter("key");
        LOG.debug(String.format("%20s: %s", "request data", requestData));
        LOG.debug(String.format("%20s: %s", "key", encodedSymKey));
        try {
            requestData = URL_CODEC.decode(requestData);
            encodedSymKey = URL_CODEC.decode(encodedSymKey);
        } catch (DecoderException e) {
            LOG.error("could not decode request parameter", e);
            throw new RequestError(HttpServletResponse.SC_BAD_REQUEST, "could not decode request parameter");
        }
        LOG.debug(String.format("%20s: %s", "decoded data", requestData));
        LOG.debug(String.format("%20s: %s", "decodec key", encodedSymKey));

        return decryptRequestData(requestData, encodedSymKey);
    }

    /**
     * The parameter <code>requestData</code> contains encrypted information and the <code>key</code> contains a
     * symmetric key with which the request data is encrypted. These two parameters are decrypted, decoded and returned
     * in a new <code>DecryptedRequestData</code> object if decryption was successful, <code>null</code> otherwise.
     */
    private DecryptedRequestData decryptRequestData(String requestData, String encodedSymKey) throws RequestError {
        DecryptedRequestData result;

        // validate initial data
        if (requestData == null || encodedSymKey == null) {
            return null;
        }

        // look up if local private key has changed
        if (privateKeyFile.lastModified() > lastReadPrivateKey) {
            try {
                readPrivateKey();
            } catch (IOException | GeneralSecurityException e) {
                LOG.error("could not read private key", e);
                throw new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error occured.");
            }
        }

        try {
            byte[] symKey = crypter.decrypt(privateKey, RSA_KEY_ALGORITHM_NAME, encodedSymKey);
            byte[] decryption = crypter.decrypt(RC4_ALGORITHM_NAME, symKey, Base64.decodeBase64(requestData));
            requestData = IOUtils.toString(decryption, "UTF-8");

            LOG.debug("requestData: " + requestData);

            result = new DecryptedRequestData();
            result.setRequestData(requestData);
            result.setSymkey(symKey);
        } catch (Exception e) {
            LOG.error("could not decrypt data -> " + e.getMessage(), e);
            throw new RequestError(HttpServletResponse.SC_BAD_REQUEST, "invalid request data");
        }
        return result;
    }

    @Override
    public XMLStreamResponse createResponseObject(String data, byte[] symKey) throws IOException {

        // refresh moodles public key if needed
        if (moodlePublicKeyFile.lastModified() > lastReadMoodlesPublicKey)
            readMoodlesPublicKey();

        Moodle moodle = new Moodle();

        try {
            String encryptedData = crypter.encrypt(symKey, RC4_ALGORITHM_NAME, RC4_ALGORITHM_NAME, data);
            String encryptedSymKey = crypter.encrypt(moodlePublicKey, RSA_KEY_ALGORITHM_NAME, symKey);

            moodle.setData(encryptedData);
            moodle.setSymKey(encryptedSymKey);
        } catch (GeneralSecurityException e) {
            LOG.error("could not encrypt data ", e);
        }

        return new XMLStreamResponse(moodle);
    }

    private void readPrivateKey() throws IOException, GeneralSecurityException {
        privateKey = crypter.readPrivatePEMKey(privateKeyFile, config.getString("semapp.private.key.passphrase"));
        lastReadPrivateKey = System.currentTimeMillis();
        LOG.info("refreshed rc private key at " + new Date(lastReadPrivateKey));
    }

    private void readMoodlesPublicKey() throws IOException {

        moodlePublicKey = crypter.readPublicPEMKey(moodlePublicKeyFile);
        lastReadMoodlesPublicKey = System.currentTimeMillis();
        LOG.info("refreshed moodles public key at " + new Date(lastReadMoodlesPublicKey));
    }

    public String cacheResourceRequest(ResourceRequest requestData) {
        return cache.addRequest(requestData);
    }

    @Override
    public ResourceRequest getResourceRequest(String sessionID) {
        return cache.getDataForSid(sessionID);
    }
}
