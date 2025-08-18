package com.crumoria.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file_conversions")
public class FileConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    @NotNull
    private UUID userId;

    @Column(name = "original_filename", nullable = false)
    @NotEmpty
    private String originalFilename;

    @Column(name = "original_format", nullable = false)
    @NotEmpty
    private String originalFormat;

    @Column(name = "target_format", nullable = false)
    @NotEmpty
    private String targetFormat;

    @Column(name = "original_file_size", nullable = false)
    @NotNull
    private Long originalFileSize;

    @Column(name = "converted_file_size")
    private Long convertedFileSize;

    @Column(name = "original_file_path", nullable = false)
    @NotEmpty
    private String originalFilePath;

    @Column(name = "converted_file_path")
    private String convertedFilePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    @Builder.Default
    private ConversionStatus status = ConversionStatus.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    @SuppressWarnings("unused")
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    @SuppressWarnings("unused")
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ConversionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}