package com.phototransformation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "photo")
public class PhotoConfiguration {
    
    private Resize resize = new Resize();
    private Upload upload = new Upload();
    private Storage storage = new Storage();
    private Imagemagick imagemagick = new Imagemagick();
    
    public static class Resize {
        private Integer maxWidth;
        private Integer maxHeight;
        
        public Integer getMaxWidth() {
            return maxWidth;
        }
        
        public void setMaxWidth(Integer maxWidth) {
            this.maxWidth = maxWidth;
        }
        
        public Integer getMaxHeight() {
            return maxHeight;
        }
        
        public void setMaxHeight(Integer maxHeight) {
            this.maxHeight = maxHeight;
        }
    }
    
    public static class Upload {
        private Integer maxSize;
        private String allowedFormats;
        
        public Integer getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
        }
        
        public String getAllowedFormats() {
            return allowedFormats;
        }
        
        public void setAllowedFormats(String allowedFormats) {
            this.allowedFormats = allowedFormats;
        }
    }
    
    public static class Storage {
        private String path;
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
    }
    
    public static class Imagemagick {
        private String path;
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
    }
    
    // Getters and setters
    public Resize getResize() {
        return resize;
    }
    
    public void setResize(Resize resize) {
        this.resize = resize;
    }
    
    public Upload getUpload() {
        return upload;
    }
    
    public void setUpload(Upload upload) {
        this.upload = upload;
    }
    
    public Storage getStorage() {
        return storage;
    }
    
    public void setStorage(Storage storage) {
        this.storage = storage;
    }
    
    public Imagemagick getImagemagick() {
        return imagemagick;
    }
    
    public void setImagemagick(Imagemagick imagemagick) {
        this.imagemagick = imagemagick;
    }
}