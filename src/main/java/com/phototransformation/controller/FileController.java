package com.phototransformation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.phototransformation.controller.service.PhotoService;
import com.phototransformation.dto.PhotoUploadResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
@Tag(name = "Photo Management", description = "API for uploading, processing, and managing photos")
public class FileController {

    @Autowired
    private PhotoService photoService;

    @Operation(summary = "Upload multiple photos", description = "Upload one or more photos. Supports PNG and JPG formats. Maximum size: 5000x5000 pixels. Images will be automatically resized based on configuration and encrypted before storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photos uploaded successfully"), 
            @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
            @ApiResponse(responseCode = "413", description = "File size exceeds limit"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoUploadResponseDTO> uploadPhotos(
            @RequestPart("files") MultipartFile[] files) {
        return photoService.upload(files);
    }

    @Operation(summary = "Download photo", description = "Download the actual photo file (decrypted) as an attachment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo downloaded successfully", content = @Content(mediaType = "image/png, image/jpeg")),
            @ApiResponse(responseCode = "404", description = "Photo not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadPhoto(
            @Parameter(description = "Photo name", required = true, example = "weddingPhoto") @PathVariable String fileName) {
     return photoService.downloadFile(fileName);
    }

    @Operation(summary = "Download all photos as ZIP", description = "Download all photos as a ZIP archive (decrypted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ZIP file downloaded successfully", content = @Content(mediaType = "application/zip")),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/download-all")
    public ResponseEntity<byte[]> downloadAllPhotosAsZip() {
        return photoService.downloadAllAsZip();
    }
}