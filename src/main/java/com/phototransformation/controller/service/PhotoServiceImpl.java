package com.phototransformation.controller.service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.dto.PhotoUploadResponseDTO;
import com.phototransformation.mapper.Mappers;
import com.phototransformation.service.PhotoManagerService;

@Service
public class PhotoServiceImpl implements PhotoService {

    private final PhotoManagerService managerService;

    public PhotoServiceImpl(final PhotoManagerService photoManagerService) {
        this.managerService = photoManagerService;
    }

    @Override
    public ResponseEntity<PhotoUploadResponseDTO> upload(MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(new PhotoUploadResponseDTO(false, "No files provided", null));
            }

            var savedPhotos = managerService.processAndSavePhotos(files);
            var photoDTOs = savedPhotos.stream()
                    .map(Mappers::convertToDTO)
                    .toList();

            var message = String.format("Successfully uploaded %d photo(s)", savedPhotos.size());
            var response = new PhotoUploadResponseDTO(true, message, photoDTOs);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new PhotoUploadResponseDTO(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PhotoUploadResponseDTO(false, "Upload failed: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(String fileName) {
        try {
            var photoMetadata = managerService.getPhotoMetadata(fileName);
            var photoData = managerService.getPhotoData(fileName);

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(photoMetadata.getContentType()));
            headers.setContentDispositionFormData("attachment", photoMetadata.getOriginalFileName());
            headers.setContentLength(photoData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(photoData);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<PhotoMetadataDTO>> getAllPhotosMetadata() {
        try {
            var allPhotos = managerService.getAllPhotosMetadata();
            return ResponseEntity.ok(allPhotos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadAllAsZip() {
        try {
            var allPhotos = managerService.getAllPhotosMetadata();

            if (allPhotos.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var baos = new ByteArrayOutputStream();
            var zos = new ZipOutputStream(baos);

            for (var photo : allPhotos) {
                var photoData = managerService.getPhotoData(photo.getFileName());

                var entry = new ZipEntry(photo.getOriginalFileName());
                zos.putNextEntry(entry);
                zos.write(photoData);
                zos.closeEntry();
            }

            zos.close();
            var zipData = baos.toByteArray();

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "photos.zip");
            headers.setContentLength(zipData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deletePhoto(Long id) {
        try {
            managerService.deletePhoto(id);
            return ResponseEntity.ok("Photo deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
