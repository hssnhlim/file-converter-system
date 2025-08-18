package com.crumoria.dto.file;


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

    @NotEmpty(message = "{target.format.not_empty}")
    private String targetFormat;

    @NotNull(message = "{file.not_null}")
    private byte[] file;

    @NotEmpty(message = "{orignal.filename.not_empty}")
    private String originalFilename;

    @NotEmpty(message = "{original.format.not_empty}")
    private String originalFormat;
}
