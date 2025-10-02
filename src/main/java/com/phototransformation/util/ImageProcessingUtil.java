package com.phototransformation.util;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.phototransformation.config.PhotoConfiguration;

@Component
public class ImageProcessingUtil {
    
    @Autowired
    private PhotoConfiguration photoConfiguration;
    
    /**
     * Gets image dimensions from byte array
     * @param imageData The image data
     * @return Dimension object containing width and height
     * @throws IOException if image cannot be read
     */
    public Dimension getImageDimensions(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new IOException("Unable to read image data");
            }
            return new Dimension(image.getWidth(), image.getHeight());
        }
    }
    
    /**
     * Validates image format based on content type
     * @param contentType The content type of the image
     * @return true if format is allowed, false otherwise
     */
    public boolean isValidImageFormat(String contentType) {
        var allowedFormats = photoConfiguration.getUpload().getAllowedFormats();
        return contentType != null && 
               (contentType.equals("image/png") || 
                contentType.equals("image/jpg") || 
                contentType.equals("image/jpeg")) &&
               allowedFormats.toLowerCase().contains(getFileExtension(contentType));
    }
    
    /**
     * Validates image dimensions
     * @param width Image width
     * @param height Image height
     * @return true if dimensions are within limits, false otherwise
     */
    public boolean isValidImageSize(double width, double height) {
        var maxSize = photoConfiguration.getUpload().getMaxSize();
        return width <= maxSize && height <= maxSize;
    }
    
    /**
     * Calculates new dimensions while maintaining aspect ratio
     * @param originalWidth Original image width
     * @param originalHeight Original image height
     * @return Dimension object with new dimensions, or null if no resize needed
     */
    public Dimension calculateNewDimensions(double originalWidth, double originalHeight) {
        var maxWidth = photoConfiguration.getResize().getMaxWidth();
        var maxHeight = photoConfiguration.getResize().getMaxHeight();

        if (maxWidth == null && maxHeight == null) {
            return null;
        }
        
        var scaleWidth = maxWidth != null ? (double) maxWidth / originalWidth : Double.MAX_VALUE;
        var scaleHeight = maxHeight != null ? (double) maxHeight / originalHeight : Double.MAX_VALUE;

        if (scaleWidth >= 1.0 && scaleHeight >= 1.0) {
            return null;
        }

        var scale = Math.min(scaleWidth, scaleHeight);
        
        var newWidth = (int) Math.round(originalWidth * scale);
        var newHeight = (int) Math.round(originalHeight * scale);
        
        return new Dimension(newWidth, newHeight);
    }
    
    /**
     * Resizes image using ImageMagick
     * @param imageData Original image data
     * @param newWidth Target width
     * @param newHeight Target height
     * @param outputFormat Output format (jpg, png)
     * @return Resized image data
     * @throws IOException if file operations fail
     * @throws InterruptedException if ImageMagick process is interrupted
     * @throws IM4JavaException if ImageMagick operation fails
     */
    public byte[] resizeImageWithImageMagick(byte[] imageData, int newWidth, int newHeight, String outputFormat) 
            throws IOException, InterruptedException, IM4JavaException {
        
       
        var tempInput = File.createTempFile("input_", ".tmp");
        var tempOutput = File.createTempFile("output_", "." + outputFormat);
        
        try {
            try (FileOutputStream fos = new FileOutputStream(tempInput)) {
                fos.write(imageData);
            }
            
            var imageMagickPath = photoConfiguration.getImagemagick().getPath();
            if (imageMagickPath != null && !imageMagickPath.isEmpty()) {
                System.setProperty("im4java.toolpath", new File(imageMagickPath).getParent());
            }
            
            var op = new IMOperation();
            op.addImage(tempInput.getAbsolutePath());
            op.resize(newWidth, newHeight);
            op.addImage(tempOutput.getAbsolutePath());
            
            var convert = new ConvertCmd();
            convert.run(op);
            
            try (FileInputStream fis = new FileInputStream(tempOutput)) {
                return fis.readAllBytes();
            }
            
        } finally {
            if (tempInput.exists()) {
                tempInput.delete();
            }
            if (tempOutput.exists()) {
                tempOutput.delete();
            }
        }
    }
    
    /**
     * Fallback resize method using Java's built-in graphics
     * @param imageData Original image data
     * @param newWidth Target width
     * @param newHeight Target height
     * @param outputFormat Output format
     * @return Resized image data
     * @throws IOException if image processing fails
     */
    public byte[] resizeImageWithJava(byte[] imageData, int newWidth, int newHeight, String outputFormat) 
            throws IOException {
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            var originalImage = ImageIO.read(bis);
            if (originalImage == null) {
                throw new IOException("Unable to read image data");
            }
            
            var resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            var g2d = resizedImage.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(resizedImage, outputFormat.equals("jpg") ? "jpeg" : outputFormat, baos);
                return baos.toByteArray();
            }
        }
    }
    
    /**
     * Main resize method that tries ImageMagick first, falls back to Java
     * @param imageData Original image data
     * @param newWidth Target width
     * @param newHeight Target height
     * @param contentType Original content type
     * @return Resized image data
     * @throws IOException if resize fails
     */
    public byte[] resizeImage(byte[] imageData, int newWidth, int newHeight, String contentType) throws IOException {
        var outputFormat = getFileExtension(contentType);
        
        try {
            return resizeImageWithImageMagick(imageData, newWidth, newHeight, outputFormat);
        } catch (Exception e) {
            System.out.println("ImageMagick resize failed, falling back to Java: " + e.getMessage());
            return resizeImageWithJava(imageData, newWidth, newHeight, outputFormat);
        }
    }
    
    /**
     * Extracts file extension from content type
     * @param contentType The content type
     * @return File extension (jpg, png)
     */
    private String getFileExtension(String contentType) {
        if (contentType == null) return "jpg";
        switch (contentType.toLowerCase()) {
            case "image/png":
                return "png";
            case "image/jpg":
            case "image/jpeg":
                return "jpg";
            default:
                return "jpg";
        }
    }
}