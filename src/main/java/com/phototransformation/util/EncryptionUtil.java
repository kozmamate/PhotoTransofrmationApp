package com.phototransformation.util;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    
    @Autowired
    private SecretKeyGenerator secretKeyGenerator;
    
    /**
     * Encrypts data using AES encryption
     * @param data The data to encrypt
     * @return The encrypted data with IV prepended
     * @throws Exception if encryption fails
     */
    public byte[] encrypt(byte[] data) throws Exception {
        var secretKey = secretKeyGenerator.loadOrGenerateSecretKey();
        
        var cipher = Cipher.getInstance(TRANSFORMATION);
        
        // Generate random IV
        var iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        var ivSpec = new IvParameterSpec(iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        var encryptedData = cipher.doFinal(data);
        
        // Prepend IV to encrypted data
        var encryptedWithIv = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, encryptedWithIv, IV_LENGTH, encryptedData.length);
        
        return encryptedWithIv;
    }
    
    /**
     * Decrypts data using AES decryption
     * @param encryptedDataWithIv The encrypted data with IV prepended
     * @return The decrypted data
     * @throws Exception if decryption fails
     */
    public byte[] decrypt(byte[] encryptedDataWithIv) throws Exception {
        var secretKey = secretKeyGenerator.loadOrGenerateSecretKey();
        
        // Extract IV from the beginning of the data
        var iv = Arrays.copyOfRange(encryptedDataWithIv, 0, IV_LENGTH);
        var encryptedData = Arrays.copyOfRange(encryptedDataWithIv, IV_LENGTH, encryptedDataWithIv.length);
        
        var cipher = Cipher.getInstance(TRANSFORMATION);
        var ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        
        return cipher.doFinal(encryptedData);
    }
}