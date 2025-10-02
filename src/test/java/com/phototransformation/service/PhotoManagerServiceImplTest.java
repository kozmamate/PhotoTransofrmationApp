package com.phototransformation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.phototransformation.dto.PhotoMetadataDTO;
import com.phototransformation.entity.Photo;
import com.phototransformation.repository.PhotoRepository;
import com.phototransformation.util.EncryptionUtil;
import com.phototransformation.util.ImageProcessingUtil;

@ExtendWith(MockitoExtension.class)
class PhotoManagerServiceImplTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private ImageProcessingUtil imageProcessingUtil;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private PhotoManagerServiceImpl photoManagerService;

    private MockMultipartFile jpegFile;

    @BeforeEach
    void setUp() {
        jpegFile = new MockMultipartFile(
                "files",
                "sample.jpg",
                "image/jpeg",
                new byte[] { 1, 2, 3, 4 });
    }

    @Test
    void processAndSavePhotosShouldSkipEmptyFiles() throws Exception {
        when(imageProcessingUtil.isValidImageFormat(jpegFile.getContentType())).thenReturn(true);
        when(imageProcessingUtil.getImageDimensions(any())).thenReturn(new Dimension(1000, 800));
        when(imageProcessingUtil.isValidImageSize(anyDouble(), anyDouble())).thenReturn(true);
        when(imageProcessingUtil.calculateNewDimensions(anyDouble(), anyDouble())).thenReturn(new Dimension(500, 400));
        when(imageProcessingUtil.resizeImage(any(), anyInt(), anyInt(), eq(jpegFile.getContentType())))
                .thenReturn(new byte[] { 9, 9, 9 });
        when(encryptionUtil.encrypt(any())).thenReturn(new byte[] { 7, 7 });
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var files = new MockMultipartFile[] { jpegFile, new MockMultipartFile("files", new byte[0]) };

        var savedPhotos = photoManagerService.processAndSavePhotos(files);

        assertThat(savedPhotos).hasSize(1);
        verify(photoRepository, times(1)).save(any(Photo.class));
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void processAndSavePhotoShouldResizeWhenNeeded() throws Exception {
        when(imageProcessingUtil.isValidImageFormat(jpegFile.getContentType())).thenReturn(true);
        when(imageProcessingUtil.getImageDimensions(any())).thenReturn(new Dimension(2000, 1500));
        when(imageProcessingUtil.isValidImageSize(anyDouble(), anyDouble())).thenReturn(true);
        when(imageProcessingUtil.calculateNewDimensions(anyDouble(), anyDouble())).thenReturn(new Dimension(1000, 750));
        when(imageProcessingUtil.resizeImage(any(), eq(1000), eq(750), eq(jpegFile.getContentType())))
                .thenReturn(new byte[] { 5, 5 });
        when(encryptionUtil.encrypt(any())).thenReturn(new byte[] { 8, 8 });
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var savedPhoto = photoManagerService.processAndSavePhoto(jpegFile);

        assertThat(savedPhoto.getResizedWidth()).isEqualTo(1000d);
        assertThat(savedPhoto.getResizedHeight()).isEqualTo(750d);
        assertThat(savedPhoto.getIsProcessed()).isTrue();
        assertThat(savedPhoto.getEncryptedData()).containsExactly(8, 8);
        assertThat(savedPhoto.getProcessedAt()).isNotNull();
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void processAndSavePhotoShouldNotResizeWhenDimensionsWithinLimit() throws Exception {
        when(imageProcessingUtil.isValidImageFormat(jpegFile.getContentType())).thenReturn(true);
        when(imageProcessingUtil.getImageDimensions(any())).thenReturn(new Dimension(800, 600));
        when(imageProcessingUtil.isValidImageSize(anyDouble(), anyDouble())).thenReturn(true);
        when(imageProcessingUtil.calculateNewDimensions(anyDouble(), anyDouble())).thenReturn(null);
        when(encryptionUtil.encrypt(any())).thenReturn(new byte[] { 3, 3 });
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var savedPhoto = photoManagerService.processAndSavePhoto(jpegFile);

        assertThat(savedPhoto.getResizedWidth()).isEqualTo(800d);
        assertThat(savedPhoto.getResizedHeight()).isEqualTo(600d);
        assertThat(savedPhoto.getIsProcessed()).isFalse();
        assertThat(savedPhoto.getEncryptedData()).containsExactly(3, 3);
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void getPhotoDataShouldReturnDecryptedBytes() throws Exception {
        var storedPhoto = new Photo();
        storedPhoto.setEncryptedData(new byte[] { 9, 4 });

        when(photoRepository.findByFileName("sample"))
                .thenReturn(storedPhoto);
        when(encryptionUtil.decrypt(any()))
                .thenReturn(new byte[] { 1, 1, 2 });

        var data = photoManagerService.getPhotoData("sample");

        assertThat(data).containsExactly(1, 1, 2);
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void getPhotoDataShouldThrowWhenPhotoMissing() {
        when(photoRepository.findByFileName("missing")).thenReturn(null);

        assertThatThrownBy(() -> photoManagerService.getPhotoData("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Photo not found");
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void getPhotoMetadataShouldReturnDto() throws Exception {
        var storedPhoto = new Photo();
        storedPhoto.setId(42L);
        storedPhoto.setFileName("stored");
        storedPhoto.setOriginalFileName("stored-original");
        storedPhoto.setContentType("image/jpeg");

        when(photoRepository.findByFileName("stored")).thenReturn(storedPhoto);

        var dto = photoManagerService.getPhotoMetadata("stored");

        assertThat(dto).isInstanceOf(PhotoMetadataDTO.class);
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getFileName()).isEqualTo("stored");
        assertThat(dto.getOriginalFileName()).isEqualTo("stored-original");
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void getPhotoMetadataShouldThrowWhenMissing() {
        when(photoRepository.findByFileName("missing")).thenReturn(null);

        assertThatThrownBy(() -> photoManagerService.getPhotoMetadata("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Photo not found");
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void getAllPhotosMetadataShouldConvertEntities() {
        var photo = new Photo();
        photo.setId(1L);
        photo.setFileName("file");

        when(photoRepository.findAll()).thenReturn(List.of(photo));

        var metadata = photoManagerService.getAllPhotosMetadata();

        assertThat(metadata).hasSize(1);
        assertThat(metadata.get(0).getId()).isEqualTo(1L);
        assertThat(metadata.get(0).getFileName()).isEqualTo("file");
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void deletePhotoShouldRemoveWhenExists() throws Exception {
        when(photoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(photoRepository).deleteById(1L);

        photoManagerService.deletePhoto(1L);

        verify(photoRepository).deleteById(1L);
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

    @Test
    void deletePhotoShouldThrowWhenMissing() {
        when(photoRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> photoManagerService.deletePhoto(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Photo not found");
        verifyNoMoreInteractions(photoRepository, imageProcessingUtil, encryptionUtil);
    }

}
