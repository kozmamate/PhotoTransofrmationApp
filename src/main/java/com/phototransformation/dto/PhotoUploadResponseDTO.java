package com.phototransformation.dto;

import java.util.List;

public class PhotoUploadResponseDTO {
    
    private boolean success;
    private String message;
    private List<PhotoMetadataDTO> uploadedPhotos;
    private int totalUploaded;
    
    public PhotoUploadResponseDTO() {}
    
    public PhotoUploadResponseDTO(boolean success, String message, List<PhotoMetadataDTO> uploadedPhotos) {
        this.success = success;
        this.message = message;
        this.uploadedPhotos = uploadedPhotos;
        this.totalUploaded = uploadedPhotos != null ? uploadedPhotos.size() : 0;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<PhotoMetadataDTO> getUploadedPhotos() {
        return uploadedPhotos;
    }
    
    public void setUploadedPhotos(List<PhotoMetadataDTO> uploadedPhotos) {
        this.uploadedPhotos = uploadedPhotos;
        this.totalUploaded = uploadedPhotos != null ? uploadedPhotos.size() : 0;
    }
    
    public int getTotalUploaded() {
        return totalUploaded;
    }
    
    public void setTotalUploaded(int totalUploaded) {
        this.totalUploaded = totalUploaded;
    }
}