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
public class FileConversionStatusResponse {

    private UUID id;
    private ConversionStatus status;
    private String errorMessage;
    private Long processingTimeMs;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String downloadUrl;
}
