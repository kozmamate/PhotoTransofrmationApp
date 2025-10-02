package com.phototransformation.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretKeyGeneratorTest {

    private SecretKeyGenerator secretKeyGenerator;
    private Path keyFilePath;
    private byte[] originalKeyContent;
    private boolean keyFileOriginallyExisted;

    @BeforeEach
    void setUp() throws IOException {
        secretKeyGenerator = new SecretKeyGenerator();
        keyFilePath = Paths.get("secretKey.key");
        if (Files.exists(keyFilePath)) {
            originalKeyContent = Files.readAllBytes(keyFilePath);
            keyFileOriginallyExisted = true;
            Files.delete(keyFilePath);
        } else {
            originalKeyContent = null;
            keyFileOriginallyExisted = false;
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (keyFileOriginallyExisted) {
            Files.write(keyFilePath, originalKeyContent);
        } else {
            Files.deleteIfExists(keyFilePath);
        }
    }

    @Test
    void loadOrGenerateSecretKeyShouldCreateFileWhenMissing() throws NoSuchAlgorithmException, IOException {
        var firstCall = secretKeyGenerator.loadOrGenerateSecretKey();

        assertThat(firstCall).isNotNull();
        assertThat(Files.exists(keyFilePath)).isTrue();

        var secondCall = secretKeyGenerator.loadOrGenerateSecretKey();

        assertThat(secondCall).isNotNull();
        assertThat(secondCall.getEncoded()).containsExactly(firstCall.getEncoded());
    }
}
