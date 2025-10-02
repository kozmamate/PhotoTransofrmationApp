package com.phototransformation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PHOTOS")
public class Photo {
   /*   Ezt az entity packaget alap esetben egy persistence/persistence-api modul alá
     szervezném. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ORIGINAL_FILE_NAME", nullable = false)
    private String originalFileName;

    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Column(name = "CONTENT_TYPE", nullable = false)
    private String contentType;

    @Column(name = "FILE_SIZE", nullable = false)
    private Long fileSize;

    @Column(name = "ORIGINAL_WIDTH", nullable = false)
    private Double originalWidth;

    @Column(name = "ORIGINAL_HEIGHT", nullable = false)
    private Double originalHeight;

    @Column(name = "RESIZED_WIDTH")
    private Double resizedWidth;

    @Column(name = "RESIZED_HEIGHT")
    private Double resizedHeight;

    @Lob
    @Column(name = "ENCRYPTED_DATA", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] encryptedData;

    @Column(name = "UPLOADED_AT", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "PROCESSED_AT")
    private LocalDateTime processedAt;

    @Column(name = "IS_PROCESSED", nullable = false)
    private Boolean isProcessed = false;

    public Photo() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Double getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(Double originalWidth) {
        this.originalWidth = originalWidth;
    }

    public Double getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(Double originalHeight) {
        this.originalHeight = originalHeight;
    }

    public Double getResizedWidth() {
        return resizedWidth;
    }

    public void setResizedWidth(Double resizedWidth) {
        this.resizedWidth = resizedWidth;
    }

    public Double getResizedHeight() {
        return resizedHeight;
    }

    public void setResizedHeight(Double resizedHeight) {
        this.resizedHeight = resizedHeight;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }
}