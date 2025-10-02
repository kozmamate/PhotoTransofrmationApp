package com.phototransformation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.zip.ZipInputStream;

import javax.crypto.KeyGenerator;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phototransformation.dto.PhotoUploadResponseDTO;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Path secretKeyPath;
    private byte[] originalSecretKey;
    private boolean secretKeyExistedBefore;

    @BeforeAll
    void ensureSecretKeyExists() throws Exception {
        secretKeyPath = Path.of("secretKey.key");
        if (Files.exists(secretKeyPath)) {
            secretKeyExistedBefore = true;
            originalSecretKey = Files.readAllBytes(secretKeyPath);
        } else {
            secretKeyExistedBefore = false;
            createSecretKey(secretKeyPath);
        }
    }

    @AfterAll
    void restoreSecretKeyState() throws IOException {
        if (secretKeyExistedBefore) {
            Files.write(secretKeyPath, originalSecretKey);
        } else {
            Files.deleteIfExists(secretKeyPath);
        }
    }

    @Test
    void uploadDownloadFlowShouldWorkEndToEnd() throws Exception {
        var imageBytes = createPngImageBytes(3000, 2000);

        var uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(new MockMultipartFile("files", "test-image.png", "image/png", imageBytes)))
                .andExpect(status().isOk())
                .andReturn();

        var uploadResponse = objectMapper.readValue(uploadResult.getResponse().getContentAsString(),
                PhotoUploadResponseDTO.class);

        assertThat(uploadResponse.isSuccess()).isTrue();
        assertThat(uploadResponse.getUploadedPhotos()).hasSize(1);

        var uploadedPhoto = uploadResponse.getUploadedPhotos().get(0);
        assertThat(uploadedPhoto.getIsProcessed()).isTrue();
        assertThat(uploadedPhoto.getProcessedAt()).isNotNull();
        assertThat(uploadedPhoto.getResizedWidth()).isLessThan(uploadedPhoto.getOriginalWidth());
        assertThat(uploadedPhoto.getResizedHeight()).isLessThan(uploadedPhoto.getOriginalHeight());

        var storedFileName = uploadedPhoto.getFileName();

        var downloadResult = mockMvc.perform(get("/api/files/download/" + storedFileName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andReturn();

        var downloaded = downloadResult.getResponse().getContentAsByteArray();
        assertThat(downloaded).isNotEmpty();

        var downloadedImage = ImageIO.read(new ByteArrayInputStream(downloaded));
        assertThat(downloadedImage).isNotNull();
        assertThat(downloadedImage.getWidth()).isEqualTo(uploadedPhoto.getResizedWidth().intValue());
        assertThat(downloadedImage.getHeight()).isEqualTo(uploadedPhoto.getResizedHeight().intValue());

        var zipResult = mockMvc.perform(get("/api/files/download-all"))
                .andExpect(status().isOk())
                .andReturn();

        var zipBytes = zipResult.getResponse().getContentAsByteArray();
        assertThat(zipBytes.length).isGreaterThan(0);

        try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            var entry = zis.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.getName()).isEqualTo(uploadedPhoto.getOriginalFileName());
            var extracted = zis.readAllBytes();
            assertThat(extracted).isNotEmpty();
        }
    }

    private void createSecretKey(Path path) throws GeneralSecurityException, IOException {
        var keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        var secretKey = keyGenerator.generateKey();
        var encoded = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        Files.writeString(path, encoded, StandardCharsets.UTF_8);
    }

    private byte[] createPngImageBytes(int width, int height) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }
}
