package com.crumoria.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.crumoria.entity.FileConversion;
import com.crumoria.entity.FileConversion.ConversionStatus;

@Repository
public interface FileConversionRepository extends 
JpaRepository<FileConversion, UUID> {

    List<FileConversion> findByUserId(UUID userId);

    Page<FileConversion> findByUserId(UUID userId, Pageable pageable);

    List<FileConversion> findByUserIdAndStatus(UUID userId, ConversionStatus status);

    @Query("SELECT fc FROM FileConversion fc WHERE fc.userId = :userId AND fc.createdAt >= :since")
    List<FileConversion> findByUserIdAndCreatedAfter(@Param("userId") UUID userId, 
        @Param("since") java.time.LocalDateTime since);

    @Query("SELECT COUNT(fc) FROM FileConversion fc WHERE fc.userId = :userId AND fc.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, 
        @Param("status") ConversionStatus status);

    @Query("SELECT fc FROM FileConversion fc WHERE fc.status IN ('PENDING', 'PROCESSING') ORDER BY fc.createdAt ASC")
    List<FileConversion> findPendingAndProcessingConversion();

    boolean existsByUserIdAndOriginalFilenameAndStatus(UUID userId, String originalFilename, ConversionStatus status);
}
