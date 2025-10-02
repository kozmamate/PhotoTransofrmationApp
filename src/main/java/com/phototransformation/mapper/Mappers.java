package com.phototransformation.mapper;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.entity.Photo;

public class Mappers {

    private Mappers() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Convert Photo entity to DTO
     */
    public static PhotoMetadataDTO convertToDTO(Photo photo) {
        var dto = new PhotoMetadataDTO();
        dto.setId(photo.getId());
        dto.setOriginalFileName(photo.getOriginalFileName());
        dto.setFileName(photo.getFileName());
        dto.setContentType(photo.getContentType());
        dto.setFileSize(photo.getFileSize());
        dto.setOriginalWidth(photo.getOriginalWidth());
        dto.setOriginalHeight(photo.getOriginalHeight());
        dto.setResizedWidth(photo.getResizedWidth());
        dto.setResizedHeight(photo.getResizedHeight());
        dto.setUploadedAt(photo.getUploadedAt());
        dto.setProcessedAt(photo.getProcessedAt());
        dto.setIsProcessed(photo.getIsProcessed());
        return dto;
    }
}
