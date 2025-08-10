package com.crumoria.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.crumoria.dto.FileConversionRequest;
import com.crumoria.dto.FileConversionResponse;
import com.crumoria.dto.FileConversionStatusResponse;

public interface FileConverterService {

    /**
     * Convert a file to the specified target format
     */
    FileConversionResponse convertFile(FileConversionRequest request, UUID userId);

    /**
     * Convert a file using MultipartFile (for direct file uploads)
     */
    FileConversionResponse convertFile(MultipartFile file, String targetFormat, UUID userId);

    /**
     * Get the status of a file conversion
     */
    FileConversionStatusResponse getConversionStatus(UUID conversionId, UUID userId);

    /**
     * Get all conversions for a user
     */
    List<FileConversionResponse> getUserConversions(UUID userId);

    /**
     * Get paginated conversions for a user
     */
    Page<FileConversionResponse> getUserConversions(UUID userId, Pageable pageable);

    /**
     * Cancel a pending conversion
     */
    void cancelConversion(UUID conversionId, UUID userId);

    /**
     * Download a converted file
     */
    byte[] downloadConvertedFile(UUID conversionId, UUID userId);

    /**
     * Get supported conversion formats
     */
    List<String> getSupportedFormats();

    /**
     * Check if a conversion format is supported
     */
    boolean isFormatSupported(String sourceFormat, String targetFormat);
}