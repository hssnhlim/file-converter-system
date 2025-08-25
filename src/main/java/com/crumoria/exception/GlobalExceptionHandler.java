package com.crumoria.exception;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /* ---------- Business exceptions ---------- */

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex,
            HttpServletRequest request) {
        HttpStatus status = switch (ex.getCode()) {
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USERNAME_TAKEN, EMAIL_TAKEN -> HttpStatus.CONFLICT;
            case WEAK_PASSWORD -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.BAD_REQUEST;
        };

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setType(typeUri(ex.getCode()));
        pd.setTitle(title(ex.getCode()));
        pd.setInstance(URI.create(request.getRequestURI()));
        if (ex.getDetails() != null) {
            pd.setProperty("details", ex.getDetails());
        }

        return pd;
    }

    /* ---------- Validation errors ---------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
            
        Map<String, String> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        BusinessException businessEx = new BusinessException(
                ErrorCode.VALIDATION_ERROR,
                "Request body contains invalid fields",
                fields);

        return handleBusiness(businessEx, request);
    }

    /* ---------- Fallback ---------- */

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        BusinessException businessEx = new BusinessException(
                ErrorCode.SERVER_ERROR, 
                "Internal server error");
        
        return handleBusiness(businessEx, request);
    }

    /* ---------- Helpers ---------- */

    private URI typeUri(ErrorCode code) {
        return URI.create("https://api.crumoria.com/errors/" +
                code.name().toLowerCase());
    }

    private String title(ErrorCode code) {
        return messageSource.getMessage(
                "problem." + code.name(), null,
                LocaleContextHolder.getLocale());
    }
}