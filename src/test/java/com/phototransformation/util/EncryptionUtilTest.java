package com.phototransformation.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EncryptionUtilTest {

    @Mock
    private SecretKeyGenerator secretKeyGenerator;

    private EncryptionUtil encryptionUtil;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() throws Exception {
        encryptionUtil = new EncryptionUtil();
        secretKey = new SecretKeySpec(new byte[32], "AES");

        lenient().when(secretKeyGenerator.loadOrGenerateSecretKey()).thenAnswer(invocation -> secretKey);
        ReflectionTestUtils.setField(encryptionUtil, "secretKeyGenerator", secretKeyGenerator);
    }

    @Test
    void encryptAndDecryptShouldRoundTrip() throws Exception {
        var original = "hello-world".getBytes(StandardCharsets.UTF_8);

        var encrypted = encryptionUtil.encrypt(original);
        var decrypted = encryptionUtil.decrypt(encrypted);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(original);
        assertThat(encrypted.length).isGreaterThan(original.length);
        assertThat(decrypted).isEqualTo(original);
        verify(secretKeyGenerator, times(2)).loadOrGenerateSecretKey();
        verifyNoMoreInteractions(secretKeyGenerator);
    }

    @Test
    void encryptShouldGenerateDifferentIvEachTime() throws Exception {
        var data = "same-data".getBytes(StandardCharsets.UTF_8);

        var encryptedOne = encryptionUtil.encrypt(data);
        var encryptedTwo = encryptionUtil.encrypt(data);

        var ivOne = Arrays.copyOfRange(encryptedOne, 0, 16);
        var ivTwo = Arrays.copyOfRange(encryptedTwo, 0, 16);

        assertThat(ivOne).isNotEqualTo(ivTwo);

        verify(secretKeyGenerator, times(2)).loadOrGenerateSecretKey();
        verifyNoMoreInteractions(secretKeyGenerator);
    }
}
