package com.phototransformation.dto;

import java.time.LocalDateTime;

public class PhotoMetadataDTO {
    
    private Long id;
    private String originalFileName;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Double originalWidth;
    private Double originalHeight;
    private Double resizedWidth;
    private Double resizedHeight;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private Boolean isProcessed;
    
    // Constructors
    public PhotoMetadataDTO() {}
    
    // Getters and Setters
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