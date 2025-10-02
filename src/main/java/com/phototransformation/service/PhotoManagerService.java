package com.phototransformation.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.entity.Photo;

public interface PhotoManagerService {

    List<Photo> processAndSavePhotos(MultipartFile[] files) throws Exception;

    Photo processAndSavePhoto(MultipartFile file) throws Exception;

    byte[] getPhotoData(String fileName) throws Exception;

    PhotoMetadataDTO getPhotoMetadata(String fileName) throws Exception;

    List<PhotoMetadataDTO> getAllPhotosMetadata();

    void deletePhoto(Long id) throws Exception;
}