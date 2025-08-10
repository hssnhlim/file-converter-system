package com.crumoria.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.crumoria.dto.FileConversionResponse;
import com.crumoria.dto.FileConversionStatusResponse;
import com.crumoria.service.FileConverterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/file-converter")
@RequiredArgsConstructor
@Tag(name = "File Converter", description = "File format conversion operations")
public class FileConverterController {

    private final FileConverterService fileConverterService;

    @PostMapping("/convert")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Convert a file to the specified target format")
    public ResponseEntity<FileConversionResponse> convertFile(
            @Parameter(description = "File to convert") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Target format for conversion") @RequestParam("targetFormat") String targetFormat,
            Authentication authentication) {
        
        UUID userId = extractUserId(authentication);
        FileConversionResponse response = fileConverterService.convertFile(file, targetFormat, userId);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/conversions")
    @Operation(summary = "Get all conversions for the authenticated user")
    public ResponseEntity<List<FileConversionResponse>> getUserConversions(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        List<FileConversionResponse> conversions = fileConverterService.getUserConversions(userId);
        return ResponseEntity.ok(conversions);
    }

    @GetMapping("/conversions/paginated")
    @Operation(summary = "Get paginated conversions for the authenticated user")
    public ResponseEntity<Page<FileConversionResponse>> getUserConversionsPaginated(
            Authentication authentication,
            Pageable pageable) {
        UUID userId = extractUserId(authentication);
        Page<FileConversionResponse> conversions = fileConverterService.getUserConversions(userId, pageable);
        return ResponseEntity.ok(conversions);
    }

    @GetMapping("/conversions/{conversionId}")
    @Operation(summary = "Get the status of a specific conversion")
    public ResponseEntity<FileConversionStatusResponse> getConversionStatus(
            @Parameter(description = "Conversion ID") @PathVariable UUID conversionId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        FileConversionStatusResponse status = fileConverterService.getConversionStatus(conversionId, userId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/conversions/{conversionId}/download")
    @Operation(summary = "Download a converted file")
    public ResponseEntity<byte[]> downloadConvertedFile(
            @Parameter(description = "Conversion ID") @PathVariable UUID conversionId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        byte[] fileData = fileConverterService.downloadConvertedFile(conversionId, userId);
        
        // Get the conversion details to set proper filename
        FileConversionStatusResponse status = fileConverterService.getConversionStatus(conversionId, userId);
        String filename = "converted_file." + status.getTargetFormat();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
    }

    @DeleteMapping("/conversions/{conversionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a pending conversion")
    public ResponseEntity<Void> cancelConversion(
            @Parameter(description = "Conversion ID") @PathVariable UUID conversionId,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        fileConverterService.cancelConversion(conversionId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/formats")
    @Operation(summary = "Get all supported file formats")
    public ResponseEntity<List<String>> getSupportedFormats() {
        List<String> formats = fileConverterService.getSupportedFormats();
        return ResponseEntity.ok(formats);
    }

    @GetMapping("/formats/check")
    @Operation(summary = "Check if a conversion format is supported")
    public ResponseEntity<Boolean> isFormatSupported(
            @Parameter(description = "Source format") @RequestParam("sourceFormat") String sourceFormat,
            @Parameter(description = "Target format") @RequestParam("targetFormat") String targetFormat) {
        boolean isSupported = fileConverterService.isFormatSupported(sourceFormat, targetFormat);
        return ResponseEntity.ok(isSupported);
    }

    private UUID extractUserId(Authentication authentication) {
        // This is a placeholder - in a real application, you would extract the user ID
        // from the JWT token or user details stored in the authentication object
        // For now, we'll use a default UUID - you'll need to implement this based on your auth system
        
        // Example implementation (adjust based on your UserDetails implementation):
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // return userDetails.getUserId(); // assuming you have a getUserId method
        
        // For now, returning a placeholder UUID - replace with actual implementation
        return UUID.randomUUID();
    }
}