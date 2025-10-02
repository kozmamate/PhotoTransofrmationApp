package com.phototransformation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.entity.Photo;
import com.phototransformation.mapper.Mappers;
import com.phototransformation.repository.PhotoRepository;
import com.phototransformation.util.EncryptionUtil;
import com.phototransformation.util.ImageProcessingUtil;

@Service
public class PhotoManagerServiceImpl implements PhotoManagerService {

    private final PhotoRepository photoRepository;
    private final ImageProcessingUtil imageProcessingUtil;
    private final EncryptionUtil encryptionUtil;

    public PhotoManagerServiceImpl(final PhotoRepository photoRepository,
                                   final ImageProcessingUtil imageProcessingUtil,
                                   final EncryptionUtil encryptionUtil) {
        this.photoRepository = photoRepository;
        this.imageProcessingUtil = imageProcessingUtil;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public List<Photo> processAndSavePhotos(MultipartFile[] files) throws Exception {
        var savedPhotos = new ArrayList<Photo>();

        for (var file : files) {
            if (!file.isEmpty()) {
                var photo = processAndSavePhoto(file);
                savedPhotos.add(photo);
            }
        }

        return savedPhotos;
    }

    @Override
    public Photo processAndSavePhoto(MultipartFile file) throws Exception {
        if (!imageProcessingUtil.isValidImageFormat(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file format. Only PNG and JPG are allowed.");
        }

        var originalData = file.getBytes();

        var originalDimensions = imageProcessingUtil.getImageDimensions(originalData);
        var originalWidth = originalDimensions.width;
        var originalHeight = originalDimensions.height;

        if (!imageProcessingUtil.isValidImageSize(originalWidth, originalHeight)) {
            throw new IllegalArgumentException("Image size exceeds maximum allowed dimensions (5000x5000).");
        }

        var photo = new Photo();
        photo.setOriginalFileName(file.getOriginalFilename());
        photo.setFileName(generateUniqueFileName(file.getOriginalFilename()));
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setOriginalWidth((double) originalWidth);
        photo.setOriginalHeight((double) originalHeight);

        var newDimensions = imageProcessingUtil.calculateNewDimensions(originalWidth, originalHeight);
        var finalImageData = originalData;

        if (newDimensions != null) {
            finalImageData = imageProcessingUtil.resizeImage(
                    originalData,
                    newDimensions.width,
                    newDimensions.height,
                    file.getContentType()
            );
            photo.setResizedWidth((double) newDimensions.width);
            photo.setResizedHeight((double) newDimensions.height);
            photo.setProcessedAt(LocalDateTime.now());
            photo.setIsProcessed(true);
        } else {
            photo.setResizedWidth((double) originalWidth);
            photo.setResizedHeight((double) originalHeight);
            photo.setIsProcessed(false);
        }

        var encryptedData = encryptionUtil.encrypt(finalImageData);
        photo.setEncryptedData(encryptedData);

        return photoRepository.save(photo);
    }

    @Override
    public byte[] getPhotoData(String fileName) throws Exception {
        var photo = photoRepository.findByFileName(fileName);
        if (photo == null) {
            throw new IllegalArgumentException("Photo not found with name: " + fileName);
        }

        return encryptionUtil.decrypt(photo.getEncryptedData());
    }

    @Override
    public PhotoMetadataDTO getPhotoMetadata(String fileName) throws Exception {
        var photo = photoRepository.findByFileName(fileName);
        if (photo == null) {
            throw new IllegalArgumentException("Photo not found with name: " + fileName);
        }
        return Mappers.convertToDTO(photo);
    }

    @Override
    public List<PhotoMetadataDTO> getAllPhotosMetadata() {
        return photoRepository.findAll().stream()
                .map(Mappers::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoto(Long id) throws Exception {
        if (!photoRepository.existsById(id)) {
            throw new IllegalArgumentException("Photo not found with ID: " + id);
        }
        photoRepository.deleteById(id);
    }

    private String generateUniqueFileName(String originalFilename) {
        var extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
