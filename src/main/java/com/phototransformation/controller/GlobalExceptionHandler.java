package com.phototransformation.controller;

import com.phototransformation.dto.PhotoUploadResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<PhotoUploadResponseDTO> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        var response = new PhotoUploadResponseDTO(
                false,
                "File size exceeds maximum allowed size",
                null);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PhotoUploadResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException e) {
        var response = new PhotoUploadResponseDTO(
                false,
                e.getMessage(),
                null);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PhotoUploadResponseDTO> handleGenericException(Exception e) {
        var response = new PhotoUploadResponseDTO(
                false,
                "An unexpected error occurred: " + e.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}