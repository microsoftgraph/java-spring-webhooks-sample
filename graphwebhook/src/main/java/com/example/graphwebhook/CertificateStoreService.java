// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CertificateStoreService {

    @Value("${certificate.storename}")
    private String storeName;

    @Value("${certificate.storepass}")
    private String storePassword;

    @Value("${certificate.alias}")
    private String alias;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private KeyStore getCertificateStore()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        var keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(storeName), storePassword.toCharArray());
        return keystore;
    }

    public String getBase64EncodedCertificate() {
        try {
            var keystore = getCertificateStore();
            var certificate = keystore.getCertificate(alias);
            return new String(Base64.encodeBase64(certificate.getEncoded()));
        } catch (final Exception e) {
            log.error("Error getting Base64 encoded certificate", e);
            return null;
        }
    }

    public String getCertificateId() {
        return alias;
    }

    public byte[] getEncryptionKey(final String base64encodedSymmetricKey) {
        try {
            var keystore = getCertificateStore();
            var asymmetricKey = keystore.getKey(alias, storePassword.toCharArray());
            var encryptedSymmetricKey = Base64.decodeBase64(base64encodedSymmetricKey);
            var cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, asymmetricKey);
            return cipher.doFinal(encryptedSymmetricKey);
        } catch (final Exception e) {
            log.error("Error getting encryption key", e);
            return new byte[0];
        }
    }

    public boolean isDataSignatureValid(final byte[] encryptionKey, final String encryptedData,
            final String comparisonSignature) {
        try {
            var decodedEncryptedData = Base64.decodeBase64(encryptedData);
            var mac = Mac.getInstance("HMACSHA256");
            var secretKey = new SecretKeySpec(encryptionKey, "HMACSHA256");
            mac.init(secretKey);
            var hashedData = mac.doFinal(decodedEncryptedData);
            var encodedHashedData = new String(Base64.encodeBase64(hashedData));
            return comparisonSignature.equals(encodedHashedData);
        } catch (final Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }

    public String getDecryptedData(final byte[] encryptionKey, final String encryptedData) {
        try {
            var secretKey = new SecretKeySpec(encryptionKey, "AES");
            var ivBytes = Arrays.copyOf(encryptionKey, 16);
            @SuppressWarnings("java:S3329")
            // Sonar warns that a random IV should be used for encryption
            // but we are decrypting here.
            var ivSpec = new IvParameterSpec(ivBytes);

            var cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedData)));
        } catch (final Exception e) {
            log.error("Error decrypting data", e);
            return null;
        }
    }
}
