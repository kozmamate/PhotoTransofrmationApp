package com.phototransformation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.controller.service.PhotoService;
import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.dto.PhotoUploadResponseDTO;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private PhotoService photoService;

    @InjectMocks
    private FileController controller;

    @Test
    void uploadPhotosShouldDelegateToService() {
        var files = new MultipartFile[] {
                new MockMultipartFile("files", "image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] { 1, 2, 3 })
        };

        var dto = new PhotoUploadResponseDTO(true, "ok", List.of(new PhotoMetadataDTO()));
        var serviceResponse = ResponseEntity.ok(dto);

        when(photoService.upload(files)).thenReturn(serviceResponse);

        var response = controller.uploadPhotos(files);

        assertThat(response).isSameAs(serviceResponse);
        verify(photoService, times(1)).upload(files);
        verifyNoMoreInteractions(photoService);
    }

    @Test
    void downloadPhotoShouldReturnBytesFromService() {
        var serviceResponse = ResponseEntity.ok(new byte[] { 9, 9, 9 });

        when(photoService.downloadFile("sample.jpg")).thenReturn(serviceResponse);

        var response = controller.downloadPhoto("sample.jpg");

        assertThat(response).isSameAs(serviceResponse);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).containsExactly(9, 9, 9);
        verify(photoService, times(1)).downloadFile("sample.jpg");
        verifyNoMoreInteractions(photoService);
    }

    @Test
    void downloadAllPhotosShouldReturnZipFromService() {
        var zipResponse = ResponseEntity.ok(new byte[] { 5, 5, 5 });

        when(photoService.downloadAllAsZip()).thenReturn(zipResponse);

        var response = controller.downloadAllPhotosAsZip();

        assertThat(response).isSameAs(zipResponse);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).containsExactly(5, 5, 5);
        verify(photoService, times(1)).downloadAllAsZip();
        verifyNoMoreInteractions(photoService);
    }
}
