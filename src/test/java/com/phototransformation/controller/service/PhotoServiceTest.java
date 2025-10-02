package com.phototransformation.controller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.entity.Photo;
import com.phototransformation.service.PhotoManagerServiceImpl;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoManagerServiceImpl photoManagerService;

    @InjectMocks
    private PhotoServiceImpl photoService;

    @Test
    void uploadShouldReturnBadRequestWhenFilesMissing() {
        var response = photoService.upload(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void uploadShouldReturnOkWhenFilesAreProcessed() throws Exception {
        var files = new MultipartFile[] {
                new MockMultipartFile("files", "image.jpg", "image/jpeg", new byte[] { 1, 2, 3 })
        };

        var savedPhoto = new Photo();
        savedPhoto.setFileName("generated-name.jpg");
        savedPhoto.setOriginalFileName("image.jpg");
        savedPhoto.setContentType("image/jpeg");

        when(photoManagerService.processAndSavePhotos(files)).thenReturn(List.of(savedPhoto));

        var response = photoService.upload(files);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getUploadedPhotos()).hasSize(1);
        assertThat(response.getBody().getTotalUploaded()).isEqualTo(1);
    }

    @Test
    void uploadShouldReturnBadRequestWhenValidationFails() throws Exception {
        var files = new MultipartFile[] {
                new MockMultipartFile("files", "invalid.bmp", "image/bmp", new byte[] { 1 })
        };

        when(photoManagerService.processAndSavePhotos(files)).thenThrow(new IllegalArgumentException("Invalid"));

        var response = photoService.upload(files);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void downloadFileShouldReturnBytesWhenFound() throws Exception {
        var fileName = "generated-name.jpg";
        var metadata = new PhotoMetadataDTO();
        metadata.setFileName(fileName);
        metadata.setOriginalFileName("image.jpg");
        metadata.setContentType("image/jpeg");

        var photoData = new byte[] { 1, 2, 3 };

        when(photoManagerService.getPhotoMetadata(fileName)).thenReturn(metadata);
        when(photoManagerService.getPhotoData(fileName)).thenReturn(photoData);

       var response = photoService.downloadFile(fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("image.jpg");
        assertThat(response.getBody()).isEqualTo(photoData);
    }

    @Test
    void downloadFileShouldReturnNotFoundWhenMissing() throws Exception {
        var fileName = "missing";
        when(photoManagerService.getPhotoMetadata(fileName)).thenThrow(new IllegalArgumentException("Not found"));

       var response = photoService.downloadFile(fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePhotoShouldReturnOkWhenDeletionSucceeds() throws Exception {
        var id = 1L;
        doNothing().when(photoManagerService).deletePhoto(id);

        var response = photoService.deletePhoto(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Photo deleted successfully");
    }

    @Test
    void deletePhotoShouldReturnNotFoundWhenPhotoMissing() throws Exception {
        var id = 2L;
        doThrow(new IllegalArgumentException("Not found")).when(photoManagerService).deletePhoto(id);

        var response = photoService.deletePhoto(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
