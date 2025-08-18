package com.crumoria.dto.file;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileConversionResponse {

    private UUID id;
    private UUID userId;
    private String originalFilename;
    private String originalFormat;
    private String targetFormat;
    private Long originalFileSize;
    private Long convertedFileSize;
    private ConversionStatus status;
    private String errorMessage;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
}
