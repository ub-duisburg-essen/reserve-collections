package unidue.rc.system;


import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.RSAPrivateCrtKeySpec;

/**
 * Created by nils on 15.06.15.
 */
public class CryptServiceImpl implements CryptService {
    private static final Logger LOG = Logger.getLogger(CryptServiceImpl.class.getName());

    /**
     * specifig provider to use for crypt, or <code>null</code> if the system
     * should search for a appropriate provider.
     */
    private String providerName;

    public CryptServiceImpl() {
        this.providerName = BouncyCastleProvider.PROVIDER_NAME;
        Provider provider = Security.getProvider(this.providerName);
        if (provider == null) {
            Security.addProvider(new BouncyCastleProvider());
            LOG.info("added bouncy castle provider to security with name " + BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Override
    public PrivateKey readPrivatePEMKey(File file, String passphrase) throws IOException {
        PEMParser reader = new PEMParser(new FileReader(file));
        Object readObject = reader.readObject();
        PrivateKey result = null;
        if (readObject instanceof PEMKeyPair) {
            PEMKeyPair kp = (PEMKeyPair) readObject;
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(providerName);
            result = converter.getPrivateKey(kp.getPrivateKeyInfo());
        } else if (readObject instanceof PEMEncryptedKeyPair) {
            PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) readObject;
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(passphrase.toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(providerName);
            KeyPair keyPair = converter.getKeyPair((encryptedKeyPair).decryptKeyPair(decProv));
            result = keyPair.getPrivate();
        }
        reader.close();
        return result;
    }

    @Override
    public PublicKey readPublicPEMKey(File file) throws IOException {
        PEMParser reader = new PEMParser(new FileReader(file));
        SubjectPublicKeyInfo result = (SubjectPublicKeyInfo) reader.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(providerName);
        PublicKey key = converter.getPublicKey(result);
        reader.close();
        return key;
    }

    @Override
    public String encrypt(byte[] plainKey, String keyAlgorithm, String algorithm, String input) throws GeneralSecurityException, UnsupportedEncodingException {

        return encrypt(new SecretKeySpec(plainKey, algorithm), algorithm, input);
    }

    @Override
    public String encrypt(Key key, String algorithm, String input) throws GeneralSecurityException, UnsupportedEncodingException {

        return encrypt(key, algorithm, input.getBytes("UTF-8"));
    }

    @Override
    public String encrypt(Key key, String algorithm, byte[] input) throws GeneralSecurityException {

        Cipher cipher = createCipher(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encoded = cipher.doFinal(input);
        return new String(Base64.encode(encoded));
    }

    @Override
    public byte[] decrypt(String algorithm, byte[] plainKey, byte[] cipherText) throws GeneralSecurityException {

        SecretKey skeySpec = new SecretKeySpec(plainKey, algorithm);
        Cipher cipher = createCipher(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(cipherText);
    }

    @Override
    public byte[] decrypt(Key key, String algorithm, String input) throws GeneralSecurityException {
        Cipher cipher = createCipher(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.decode(input);
        return cipher.doFinal(decoded);
    }

    @Override
    public String buildSHA256Hash(String input) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes("UTF-8"));
            byte[] digest = md.digest();
            result = Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Creates a {@link Cipher} with target algorithm and provider if is was given during initialization of this object
     */
    private Cipher createCipher(String algorithm) throws GeneralSecurityException {
        return providerName != null ? Cipher.getInstance(algorithm, providerName) : Cipher.getInstance(algorithm);
    }

    private void crypt(Key key, int mode, String algorithm, File inputFile, File outputFile) throws IOException, GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(mode, key);
        DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));

        InputStream in = new FileInputStream(inputFile);
        crypt(in, out, cipher);
        in.close();
        out.close();
    }

    /**
     * Transforms all bytes from the input stream with a crypt alorithm and
     * sends the transformed bytes to the output stream.
     *
     * @param in     input stream
     * @param out    output stream
     * @param cipher crypt alorithm used
     */
    private static void crypt(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(blockSize);
        byte[] input = new byte[blockSize];
        byte[] output = new byte[outputSize];
        int inLength = 0;
        boolean finished = false;

        while (!finished) {
            inLength = in.read(input);

            if (inLength == blockSize) {
                int outLength = cipher.update(input, 0, blockSize, output);
                out.write(output, 0, outLength);
            } else {
                finished = true;
            }
        }

        if (inLength > 0)
            output = cipher.doFinal(input, 0, inLength);
        else
            output = cipher.doFinal();

        out.write(output);
    }
}
