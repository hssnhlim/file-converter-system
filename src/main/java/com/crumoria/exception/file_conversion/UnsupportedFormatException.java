package com.crumoria.exception.file_conversion;

public class UnsupportedFormatException extends FileConversionException {

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(String sourceFormat, String targetFormat) {
        super(String.format(
            "Conversion from %s to %s is not supported",
            sourceFormat, targetFormat
        ));
    }
}
