package com.crumoria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.crumoria.dto.FileConversionResponse;
import com.crumoria.dto.FileConversionStatusResponse;
import com.crumoria.entity.FileConversion;
import com.crumoria.entity.FileConversion.ConversionStatus;
import com.crumoria.exception.FileConversionException;
import com.crumoria.exception.FileTooLargeException;
import com.crumoria.exception.UnsupportedFormatException;
import com.crumoria.repository.FileConversionRepository;
import com.crumoria.service.impl.FileConverterServiceImpl;

@ExtendWith(MockitoExtension.class)
class FileConverterServiceTest {

    @Mock
    private FileConversionRepository fileConversionRepository;

    @InjectMocks
    private FileConverterServiceImpl fileConverterService;

    private UUID testUserId;
    private FileConversion testConversion;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testConversion = FileConversion.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .originalFilename("test.pdf")
                .originalFormat("pdf")
                .targetFormat("txt")
                .originalFileSize(1024L)
                .status(ConversionStatus.PENDING)
                .originalFilePath("/tmp/test.pdf")
                .build();
    }

    @Test
    void testIsFormatSupported_ValidConversion_ReturnsTrue() {
        assertTrue(fileConverterService.isFormatSupported("pdf", "txt"));
        assertTrue(fileConverterService.isFormatSupported("docx", "pdf"));
        assertTrue(fileConverterService.isFormatSupported("jpg", "png"));
    }

    @Test
    void testIsFormatSupported_InvalidConversion_ReturnsFalse() {
        assertFalse(fileConverterService.isFormatSupported("pdf", "mp4"));
        assertFalse(fileConverterService.isFormatSupported("invalid", "txt"));
        assertFalse(fileConverterService.isFormatSupported("txt", "invalid"));
    }

    @Test
    void testGetSupportedFormats_ReturnsExpectedFormats() {
        List<String> formats = fileConverterService.getSupportedFormats();
        assertNotNull(formats);
        assertTrue(formats.contains("pdf"));
        assertTrue(formats.contains("docx"));
        assertTrue(formats.contains("txt"));
        assertTrue(formats.contains("jpg"));
    }

    @Test
    void testGetUserConversions_ReturnsUserConversions() {
        when(fileConversionRepository.findByUserId(testUserId))
                .thenReturn(Arrays.asList(testConversion));

        List<FileConversionResponse> conversions = fileConverterService.getUserConversions(testUserId);

        assertNotNull(conversions);
        assertEquals(1, conversions.size());
        assertEquals(testConversion.getId(), conversions.get(0).getId());
        verify(fileConversionRepository).findByUserId(testUserId);
    }

    @Test
    void testGetUserConversionsPaginated_ReturnsPaginatedResults() {
        Pageable pageable = mock(Pageable.class);
        Page<FileConversion> page = new PageImpl<>(Arrays.asList(testConversion));
        
        when(fileConversionRepository.findByUserId(testUserId, pageable))
                .thenReturn(page);

        Page<FileConversionResponse> result = fileConverterService.getUserConversions(testUserId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(fileConversionRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void testGetConversionStatus_ValidConversion_ReturnsStatus() {
        when(fileConversionRepository.findById(testConversion.getId()))
                .thenReturn(Optional.of(testConversion));

        FileConversionStatusResponse status = fileConverterService.getConversionStatus(testConversion.getId(), testUserId);

        assertNotNull(status);
        assertEquals(testConversion.getId(), status.getId());
        assertEquals(testConversion.getStatus(), status.getStatus());
    }

    @Test
    void testGetConversionStatus_ConversionNotFound_ThrowsException() {
        when(fileConversionRepository.findById(testConversion.getId()))
                .thenReturn(Optional.empty());

        assertThrows(FileConversionException.class, () -> {
            fileConverterService.getConversionStatus(testConversion.getId(), testUserId);
        });
    }

    @Test
    void testGetConversionStatus_WrongUser_ThrowsException() {
        UUID wrongUserId = UUID.randomUUID();
        when(fileConversionRepository.findById(testConversion.getId()))
                .thenReturn(Optional.of(testConversion));

        assertThrows(FileConversionException.class, () -> {
            fileConverterService.getConversionStatus(testConversion.getId(), wrongUserId);
        });
    }

    @Test
    void testCancelConversion_ValidConversion_UpdatesStatus() {
        when(fileConversionRepository.findById(testConversion.getId()))
                .thenReturn(Optional.of(testConversion));
        when(fileConversionRepository.save(any(FileConversion.class)))
                .thenReturn(testConversion);

        assertDoesNotThrow(() -> {
            fileConverterService.cancelConversion(testConversion.getId(), testUserId);
        });

        verify(fileConversionRepository).save(any(FileConversion.class));
    }

    @Test
    void testCancelConversion_CompletedConversion_ThrowsException() {
        testConversion.setStatus(ConversionStatus.COMPLETED);
        when(fileConversionRepository.findById(testConversion.getId()))
                .thenReturn(Optional.of(testConversion));

        assertThrows(FileConversionException.class, () -> {
            fileConverterService.cancelConversion(testConversion.getId(), testUserId);
        });
    }
}