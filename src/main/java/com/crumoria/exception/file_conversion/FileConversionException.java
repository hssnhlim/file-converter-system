package com.crumoria.exception.file_conversion;

public class FileConversionException extends RuntimeException {

    public FileConversionException(String message) {
        super(message);
    }

    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }

}
