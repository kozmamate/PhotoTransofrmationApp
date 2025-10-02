package com.phototransformation.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.phototransformation.config.PhotoConfiguration;

class ImageProcessingUtilTest {

    private ImageProcessingUtil imageProcessingUtil;

    @BeforeEach
    void setUp() {
        imageProcessingUtil = new ImageProcessingUtil();

        var configuration = new PhotoConfiguration();

        var upload = new PhotoConfiguration.Upload();
        upload.setAllowedFormats("png,jpg,jpeg");
        upload.setMaxSize(5000);
        configuration.setUpload(upload);

        var resize = new PhotoConfiguration.Resize();
        resize.setMaxWidth(1920);
        resize.setMaxHeight(1080);
        configuration.setResize(resize);

        ReflectionTestUtils.setField(imageProcessingUtil, "photoConfiguration", configuration);
    }

    @Test
    void isValidImageFormatShouldAllowConfiguredTypes() {
        assertThat(imageProcessingUtil.isValidImageFormat("image/png")).isTrue();
        assertThat(imageProcessingUtil.isValidImageFormat("image/jpeg")).isTrue();
        assertThat(imageProcessingUtil.isValidImageFormat("image/gif")).isFalse();
    }

    @Test
    void isValidImageSizeShouldRespectMaxSize() {
        assertThat(imageProcessingUtil.isValidImageSize(4000, 3000)).isTrue();
        assertThat(imageProcessingUtil.isValidImageSize(6000, 1000)).isFalse();
    }

    @Test
    void calculateNewDimensionsShouldMaintainAspectRatioWhenResizeNeeded() {
        var newDimensions = imageProcessingUtil.calculateNewDimensions(4000, 2000);

        assertThat(newDimensions).isNotNull();
        assertThat(newDimensions.width).isLessThanOrEqualTo(1920);
        assertThat(newDimensions.height).isLessThanOrEqualTo(1080);
    assertThat((double) newDimensions.width / newDimensions.height).isCloseTo(2.0, offset(0.01));
    }

    @Test
    void calculateNewDimensionsShouldReturnNullWhenWithinLimits() {
        var newDimensions = imageProcessingUtil.calculateNewDimensions(800, 600);

        assertThat(newDimensions).isNull();
    }
}
