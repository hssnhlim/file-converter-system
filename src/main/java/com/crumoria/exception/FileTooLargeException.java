package com.crumoria.exception;

public class FileTooLargeException extends FileConversionException {

    public FileTooLargeException(String message) {
        super(message);
    }

    public FileTooLargeException(long fileSize, long maxSize) {
        super(String.format("File size %d bytes exceeds maximum allowed size of %d bytes", fileSize, maxSize));
    }
}