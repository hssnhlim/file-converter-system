package com.crumoria.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.crumoria.dto.FileConversionRequest;
import com.crumoria.dto.FileConversionResponse;
import com.crumoria.dto.FileConversionStatusResponse;
import com.crumoria.entity.FileConversion;
import com.crumoria.entity.FileConversion.ConversionStatus;
import com.crumoria.exception.FileConversionException;
import com.crumoria.exception.FileTooLargeException;
import com.crumoria.exception.UnsupportedFormatException;
import com.crumoria.repository.FileConversionRepository;
import com.crumoria.service.FileConverterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileConverterServiceImpl implements FileConverterService {

    private final FileConversionRepository fileConversionRepository;

    @Value("${file.converter.max-file-size:104857600}") // 100MB default
    private long maxFileSize;

    @Value("${file.converter.storage.path:/tmp/converted-files}")
    private String storagePath;

    @Value("${file.converter.supported-formats:pdf,docx,txt,jpg,png,gif,bmp,mp4,avi,mov}")
    private String supportedFormats;

    private static final Map<String, List<String>> SUPPORTED_CONVERSIONS = new HashMap<>();
    
    static {
        // PDF conversions
        SUPPORTED_CONVERSIONS.put("pdf", Arrays.asList("txt", "docx"));
        // Word conversions
        SUPPORTED_CONVERSIONS.put("docx", Arrays.asList("pdf", "txt"));
        // Text conversions
        SUPPORTED_CONVERSIONS.put("txt", Arrays.asList("pdf", "docx"));
        // Image conversions
        SUPPORTED_CONVERSIONS.put("jpg", Arrays.asList("png", "gif", "bmp"));
        SUPPORTED_CONVERSIONS.put("png", Arrays.asList("jpg", "gif", "bmp"));
        SUPPORTED_CONVERSIONS.put("gif", Arrays.asList("jpg", "png", "bmp"));
        SUPPORTED_CONVERSIONS.put("bmp", Arrays.asList("jpg", "png", "gif"));
        // Video conversions
        SUPPORTED_CONVERSIONS.put("mp4", Arrays.asList("avi", "mov"));
        SUPPORTED_CONVERSIONS.put("avi", Arrays.asList("mp4", "mov"));
        SUPPORTED_CONVERSIONS.put("mov", Arrays.asList("mp4", "avi"));
    }

    @Override
    public FileConversionResponse convertFile(FileConversionRequest request, UUID userId) {
        validateFileSize(request.getFile().length);
        validateFormat(request.getOriginalFormat(), request.getTargetFormat());
        
        FileConversion conversion = createConversionRecord(request, userId);
        
        // Process conversion asynchronously
        processConversionAsync(conversion);
        
        return mapToResponse(conversion);
    }

    @Override
    public FileConversionResponse convertFile(MultipartFile file, String targetFormat, UUID userId) {
        try {
            validateFileSize(file.getSize());
            String originalFormat = FilenameUtils.getExtension(file.getOriginalFilename());
            validateFormat(originalFormat, targetFormat);
            
            FileConversion conversion = createConversionRecordFromMultipart(file, targetFormat, userId);
            
            // Process conversion asynchronously
            processConversionAsync(conversion);
            
            return mapToResponse(conversion);
        } catch (IOException e) {
            throw new FileConversionException("Error processing uploaded file", e);
        }
    }

    @Override
    public FileConversionStatusResponse getConversionStatus(UUID conversionId, UUID userId) {
        FileConversion conversion = getConversionByIdAndUserId(conversionId, userId);
        return mapToStatusResponse(conversion);
    }

    @Override
    public List<FileConversionResponse> getUserConversions(UUID userId) {
        return fileConversionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<FileConversionResponse> getUserConversions(UUID userId, Pageable pageable) {
        return fileConversionRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void cancelConversion(UUID conversionId, UUID userId) {
        FileConversion conversion = getConversionByIdAndUserId(conversionId, userId);
        
        if (conversion.getStatus() == ConversionStatus.COMPLETED) {
            throw new FileConversionException("Cannot cancel completed conversion");
        }
        
        conversion.setStatus(ConversionStatus.CANCELLED);
        conversion.setUpdatedAt(LocalDateTime.now());
        fileConversionRepository.save(conversion);
    }

    @Override
    public byte[] downloadConvertedFile(UUID conversionId, UUID userId) {
        FileConversion conversion = getConversionByIdAndUserId(conversionId, userId);
        
        if (conversion.getStatus() != ConversionStatus.COMPLETED) {
            throw new FileConversionException("File conversion is not completed yet");
        }
        
        try {
            Path filePath = Paths.get(conversion.getConvertedFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileConversionException("Error reading converted file", e);
        }
    }

    @Override
    public List<String> getSupportedFormats() {
        return Arrays.asList(supportedFormats.split(","));
    }

    @Override
    public boolean isFormatSupported(String sourceFormat, String targetFormat) {
        List<String> supportedTargets = SUPPORTED_CONVERSIONS.get(sourceFormat.toLowerCase());
        return supportedTargets != null && supportedTargets.contains(targetFormat.toLowerCase());
    }

    private void validateFileSize(long fileSize) {
        if (fileSize > maxFileSize) {
            throw new FileTooLargeException(fileSize, maxFileSize);
        }
    }

    private void validateFormat(String sourceFormat, String targetFormat) {
        if (!isFormatSupported(sourceFormat, targetFormat)) {
            throw new UnsupportedFormatException(sourceFormat, targetFormat);
        }
    }

    private FileConversion createConversionRecord(FileConversionRequest request, UUID userId) {
        FileConversion conversion = FileConversion.builder()
                .userId(userId)
                .originalFilename(request.getOriginalFilename())
                .originalFormat(request.getOriginalFormat())
                .targetFormat(request.getTargetFormat())
                .originalFileSize((long) request.getFile().length)
                .status(ConversionStatus.PENDING)
                .originalFilePath(saveOriginalFile(request.getFile(), request.getOriginalFilename()))
                .build();
        
        return fileConversionRepository.save(conversion);
    }

    private FileConversion createConversionRecordFromMultipart(MultipartFile file, String targetFormat, UUID userId) throws IOException {
        FileConversion conversion = FileConversion.builder()
                .userId(userId)
                .originalFilename(file.getOriginalFilename())
                .originalFormat(FilenameUtils.getExtension(file.getOriginalFilename()))
                .targetFormat(targetFormat)
                .originalFileSize(file.getSize())
                .status(ConversionStatus.PENDING)
                .originalFilePath(saveOriginalFile(file.getBytes(), file.getOriginalFilename()))
                .build();
        
        return fileConversionRepository.save(conversion);
    }

    private String saveOriginalFile(byte[] fileData, String filename) {
        try {
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            
            String uniqueFilename = UUID.randomUUID() + "_" + filename;
            Path filePath = storageDir.resolve(uniqueFilename);
            Files.write(filePath, fileData);
            
            return filePath.toString();
        } catch (IOException e) {
            throw new FileConversionException("Error saving original file", e);
        }
    }

    @Async
    protected void processConversionAsync(FileConversion conversion) {
        try {
            conversion.setStatus(ConversionStatus.PROCESSING);
            conversion.setUpdatedAt(LocalDateTime.now());
            fileConversionRepository.save(conversion);
            
            long startTime = System.currentTimeMillis();
            
            byte[] convertedData = performConversion(conversion);
            String convertedFilePath = saveConvertedFile(convertedData, conversion);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            conversion.setStatus(ConversionStatus.COMPLETED);
            conversion.setConvertedFilePath(convertedFilePath);
            conversion.setConvertedFileSize((long) convertedData.length);
            conversion.setProcessingTimeMs(processingTime);
            conversion.setCompletedAt(LocalDateTime.now());
            conversion.setUpdatedAt(LocalDateTime.now());
            
            fileConversionRepository.save(conversion);
            
        } catch (Exception e) {
            log.error("Error processing conversion {}: {}", conversion.getId(), e.getMessage(), e);
            
            conversion.setStatus(ConversionStatus.FAILED);
            conversion.setErrorMessage(e.getMessage());
            conversion.setUpdatedAt(LocalDateTime.now());
            fileConversionRepository.save(conversion);
        }
    }

    private byte[] performConversion(FileConversion conversion) throws IOException {
        String sourceFormat = conversion.getOriginalFormat().toLowerCase();
        String targetFormat = conversion.getTargetFormat().toLowerCase();
        
        byte[] originalData = Files.readAllBytes(Paths.get(conversion.getOriginalFilePath()));
        
        switch (sourceFormat) {
            case "pdf":
                return convertFromPdf(originalData, targetFormat);
            case "docx":
                return convertFromDocx(originalData, targetFormat);
            case "txt":
                return convertFromTxt(originalData, targetFormat);
            case "jpg":
            case "png":
            case "gif":
            case "bmp":
                return convertImage(originalData, sourceFormat, targetFormat);
            case "mp4":
            case "avi":
            case "mov":
                return convertVideo(originalData, sourceFormat, targetFormat);
            default:
                throw new UnsupportedFormatException(sourceFormat, targetFormat);
        }
    }

    private byte[] convertFromPdf(byte[] pdfData, String targetFormat) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            switch (targetFormat) {
                case "txt":
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    return text.getBytes();
                case "docx":
                    return convertPdfToDocx(document);
                default:
                    throw new UnsupportedFormatException("pdf", targetFormat);
            }
        }
    }

    private byte[] convertFromDocx(byte[] docxData, String targetFormat) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docxData))) {
            switch (targetFormat) {
                case "txt":
                    StringBuilder text = new StringBuilder();
                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                        text.append(paragraph.getText()).append("\n");
                    }
                    return text.toString().getBytes();
                case "pdf":
                    return convertDocxToPdf(document);
                default:
                    throw new UnsupportedFormatException("docx", targetFormat);
            }
        }
    }

    private byte[] convertFromTxt(byte[] txtData, String targetFormat) throws IOException {
        String text = new String(txtData);
        
        switch (targetFormat) {
            case "pdf":
                return convertTxtToPdf(text);
            case "docx":
                return convertTxtToDocx(text);
            default:
                throw new UnsupportedFormatException("txt", targetFormat);
        }
    }

    private byte[] convertImage(byte[] imageData, String sourceFormat, String targetFormat) throws IOException {
        // For now, return the original data as image conversion requires more complex libraries
        // In production, you would use libraries like ImageMagick, OpenCV, or similar
        log.warn("Image conversion from {} to {} not fully implemented", sourceFormat, targetFormat);
        return imageData;
    }

    private byte[] convertVideo(byte[] videoData, String sourceFormat, String targetFormat) throws IOException {
        // For now, return the original data as video conversion requires more complex libraries
        // In production, you would use libraries like FFmpeg or similar
        log.warn("Video conversion from {} to {} not fully implemented", sourceFormat, targetFormat);
        return videoData;
    }

    // Placeholder methods for actual conversion implementations
    private byte[] convertPdfToDocx(PDDocument document) throws IOException {
        // Implementation would use Apache POI or similar
        throw new UnsupportedOperationException("PDF to DOCX conversion not yet implemented");
    }

    private byte[] convertDocxToPdf(XWPFDocument document) throws IOException {
        // Implementation would use Apache POI or similar
        throw new UnsupportedOperationException("DOCX to PDF conversion not yet implemented");
    }

    private byte[] convertTxtToPdf(String text) throws IOException {
        // Implementation would use iText or similar
        throw new UnsupportedOperationException("TXT to PDF conversion not yet implemented");
    }

    private byte[] convertTxtToDocx(String text) throws IOException {
        // Implementation would use Apache POI or similar
        throw new UnsupportedOperationException("TXT to DOCX conversion not yet implemented");
    }

    private String saveConvertedFile(byte[] convertedData, FileConversion conversion) throws IOException {
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        String filename = FilenameUtils.getBaseName(conversion.getOriginalFilename()) + "." + conversion.getTargetFormat();
        String uniqueFilename = UUID.randomUUID() + "_" + filename;
        Path filePath = storageDir.resolve(uniqueFilename);
        Files.write(filePath, convertedData);
        
        return filePath.toString();
    }

    private FileConversion getConversionByIdAndUserId(UUID conversionId, UUID userId) {
        return fileConversionRepository.findById(conversionId)
                .filter(conversion -> conversion.getUserId().equals(userId))
                .orElseThrow(() -> new FileConversionException("Conversion not found"));
    }

    private FileConversionResponse mapToResponse(FileConversion conversion) {
        return FileConversionResponse.builder()
                .id(conversion.getId())
                .userId(conversion.getUserId())
                .originalFilename(conversion.getOriginalFilename())
                .originalFormat(conversion.getOriginalFormat())
                .targetFormat(conversion.getTargetFormat())
                .originalFileSize(conversion.getOriginalFileSize())
                .convertedFileSize(conversion.getConvertedFileSize())
                .status(conversion.getStatus())
                .errorMessage(conversion.getErrorMessage())
                .processingTimeMs(conversion.getProcessingTimeMs())
                .createdAt(conversion.getCreatedAt())
                .updatedAt(conversion.getUpdatedAt())
                .completedAt(conversion.getCompletedAt())
                .downloadUrl(conversion.getStatus() == ConversionStatus.COMPLETED ? 
                    "/api/v1/file-converter/" + conversion.getId() + "/download" : null)
                .build();
    }

    private FileConversionStatusResponse mapToStatusResponse(FileConversion conversion) {
        return FileConversionStatusResponse.builder()
                .id(conversion.getId())
                .status(conversion.getStatus())
                .errorMessage(conversion.getErrorMessage())
                .processingTimeMs(conversion.getProcessingTimeMs())
                .updatedAt(conversion.getUpdatedAt())
                .completedAt(conversion.getCompletedAt())
                .downloadUrl(conversion.getStatus() == ConversionStatus.COMPLETED ? 
                    "/api/v1/file-converter/" + conversion.getId() + "/download" : null)
                .build();
    }
}