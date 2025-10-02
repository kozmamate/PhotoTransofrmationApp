package com.phototransformation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phototransformation.entity.Photo;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    List<Photo> findByIsProcessed(Boolean isProcessed);
    
    List<Photo> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Photo> findByOriginalFileNameContaining(String fileName);
    
    Photo findByFileName(String fileName);
}