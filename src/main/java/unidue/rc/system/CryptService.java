package unidue.rc.system;

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

import org.bouncycastle.openssl.PEMParser;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * Through an instance of <code>CryptService</code> one is able to encrypt and decrypt
 * data with the use of {@link Key} elements. It is recommended to use bouncy
 * castles provider to encrypt and decrypt for better support of different
 * algorithms.
 * Created by nils on 15.06.15.
 */
public interface CryptService {

    /**
     * Reads a openssl generated private key with the use of {@link PEMParser}.
     *
     * @param file       reference to openssl generated private key file
     * @param passphrase passphrase for key file if it is encrypted, otherwise it can be <code>null</code>
     * @return the read private key
     * @throws GeneralSecurityException generic error thrown on security issues during encrypt
     * @throws IOException              thrown if the file could not be read
     */
    PrivateKey readPrivatePEMKey(File file, final String passphrase) throws IOException, GeneralSecurityException;

    /**
     * Reads a openssl generated {@link PublicKey}.
     *
     * @param file contains the public key file that should be read
     * @return the read public key
     * @throws IOException thrown if the file could not be read
     */
    PublicKey readPublicPEMKey(File file) throws IOException;

    /**
     * Encrypts target input string with target key and encodes the encrypted
     * result with base64 encoding.
     *
     * @param plainKey     key with is read with target algorithm
     * @param keyAlgorithm the name of the secret-key algorithm to be associated with the
     *                     given key material.
     * @param algorithm    algorithm which is user for encryption
     * @param input        text, which should be encrypted and encoded
     * @return encoded and encrypted input
     * @throws GeneralSecurityException     generic error thrown on security issues during encrypt
     * @throws UnsupportedEncodingException thrown if the used encoding is not supported
     */
    String encrypt(byte[] plainKey, String keyAlgorithm, String algorithm, String input) throws GeneralSecurityException, UnsupportedEncodingException;

    /**
     * See {@link CryptService#encrypt(byte[], String, String, String)}
     *
     * @param key       key with is read with target algorithm
     * @param algorithm algorithm which is user for encryption
     * @param input     text, which should be encrypted and encoded
     * @return encoded and encrypted input
     * @throws GeneralSecurityException     generic error thrown on security issues during encrypt
     * @throws UnsupportedEncodingException thrown if the used encoding is not supported
     */
    String encrypt(Key key, String algorithm, String input) throws GeneralSecurityException, UnsupportedEncodingException;

    /**
     * See {@link CryptService#encrypt(byte[], String, String, String)}
     *
     * @param key       key with is read with target algorithm
     * @param algorithm algorithm which is user for encryption
     * @param input     text, which should be encrypted and encoded
     * @return encoded and encrypted input
     * @throws GeneralSecurityException generic error thrown on security issues during encrypt
     */
    String encrypt(Key key, String algorithm, byte[] input) throws GeneralSecurityException;

    /**
     * Decrypts target input with given key and algorithm. The cipher is the pure encoded, encrypted input.
     *
     * @param algorithm  algorithm which is user for decryption
     * @param plainKey   key to use for decryption
     * @param cipherText input to decrypt
     * @return the decrypted bytes
     * @throws GeneralSecurityException     thrown if decryption could not be executed
     */
    byte[] decrypt(String algorithm, byte[] plainKey, byte[] cipherText) throws GeneralSecurityException;

    /**
     * Decrypts target input with given key and algorithm. The cipher is the pure encoded, encrypted input.
     *
     * @param key       key to use for decryption
     * @param algorithm algorithm which is user for decryption
     * @param input     input to decrypt
     * @return the decrypted bytes
     * @throws GeneralSecurityException     thrown if decryption could not be executed
     * @throws UnsupportedEncodingException thrown if decrypted input could not encoded with utf-8 encoding
     */
    byte[] decrypt(Key key, String algorithm, String input) throws UnsupportedEncodingException, GeneralSecurityException;

    /**
     * Creates SHA-256 has string from target input.
     *
     * @param input string to create hash for
     * @return a 64byte String with the hashed input.
     */
    String buildSHA256Hash(String input);

}
