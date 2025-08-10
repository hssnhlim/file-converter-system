package com.crumoria.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileConversionRequest {

    @NotEmpty(message = "Target format is required")
    private String targetFormat;
    
    @NotNull(message = "File is required")
    private byte[] file;
    
    @NotEmpty(message = "Original filename is required")
    private String originalFilename;
    
    @NotEmpty(message = "Original format is required")
    private String originalFormat;
}