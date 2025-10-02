package com.phototransformation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class SecretKeyGenerator {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_FILE = "secretKey.key";
    private static final int KEY_LENGTH = 256;

    /**
     * Loads the secret key from the file system, generates a new one if not found
     * 
     * @return The loaded or newly generated SecretKey
     * @throws NoSuchAlgorithmException if AES algorithm is not available
     * @throws IOException              if file operations fail
     */
    public SecretKey loadOrGenerateSecretKey() throws NoSuchAlgorithmException, IOException {
        var keyPath = Paths.get(SECRET_KEY_FILE);

        if (Files.exists(keyPath)) {
            return loadSecretKeyFromFile();
        } else {
            return generateAndSaveSecretKey();
        }
    }

    /**
     * Generates a new AES secret key and saves it to the file system
     * 
     * @return The generated SecretKey
     * @throws NoSuchAlgorithmException if AES algorithm is not available
     * @throws IOException              if file operations fail
     */
    private SecretKey generateAndSaveSecretKey() throws NoSuchAlgorithmException, IOException {
        var keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_LENGTH, new SecureRandom());

        var secretKey = keyGenerator.generateKey();
        saveSecretKeyToFile(secretKey);

        return secretKey;
    }

    /**
     * Saves the secret key to a file
     * 
     * @param secretKey The secret key to save
     * @throws IOException if file operations fail
     */
    private void saveSecretKeyToFile(SecretKey secretKey) throws IOException {
        var encoded = secretKey.getEncoded();
        var encodedKey = Base64.getEncoder().encodeToString(encoded);

        try (FileWriter writer = new FileWriter(SECRET_KEY_FILE)) {
            writer.write(encodedKey);
        }

        System.out.println("Secret key saved to: " + new File(SECRET_KEY_FILE).getAbsolutePath());
    }

    /**
     * Loads the secret key from a file
     * 
     * @return The loaded SecretKey
     * @throws IOException if file operations fail
     */
    private SecretKey loadSecretKeyFromFile() throws IOException {
        String encodedKey;
        try (BufferedReader reader = new BufferedReader(new FileReader(SECRET_KEY_FILE))) {
            encodedKey = reader.readLine();
        }

        var decodedKey = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(decodedKey, ALGORITHM);
    }
}