package com.phototransformation.controller.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.dto.PhotoUploadResponseDTO;

public interface PhotoService {

    ResponseEntity<PhotoUploadResponseDTO> upload(MultipartFile[] files);

    ResponseEntity<byte[]> downloadFile(String fileName);

    ResponseEntity<List<PhotoMetadataDTO>> getAllPhotosMetadata();

    ResponseEntity<byte[]> downloadAllAsZip();

    ResponseEntity<String> deletePhoto(Long id);
}
