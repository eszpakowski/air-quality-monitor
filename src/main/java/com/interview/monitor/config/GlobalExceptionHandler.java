package com.interview.monitor.config;

import com.interview.monitor.adapters.inbound.rest.dto.ResponseDTO;
import com.interview.monitor.domain.exception.DatastoreException;
import com.interview.monitor.domain.exception.IntegrationException;
import com.interview.monitor.domain.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    //Custom 4xx Exceptions
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseDTO> handleValidationException(ValidationException ex) {
        log.warn("ValidationException thrown:", ex);
        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, ex.getMessage()));
    }

    // Standard 4xx Exceptions
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ResponseDTO> handleMissingServletRequestPartException(MissingServletRequestParameterException ex) {
        log.warn("MissingServletRequestParameterException thrown:", ex);
        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException thrown:", ex);

        var sb = constructValidationExceptionMsg(ex);

        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, sb.toString()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseDTO> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.warn("HttpMediaTypeNotSupportedException thrown:", ex);
        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("MethodArgumentTypeMismatchException thrown:", ex);
        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException thrown:", ex);
        return ResponseEntity
                .status(BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(BAD_REQUEST, ex.getMessage()));
    }

    //Custom 5xx Exceptions
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ResponseDTO> handleIntegrationException(IntegrationException ex) {
        log.warn("IntegrationException thrown:", ex);
        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(SERVICE_UNAVAILABLE, ex.getMessage()));
    }

    @ExceptionHandler(DatastoreException.class)
    public ResponseEntity<ResponseDTO> handleDatastoreException(DatastoreException ex) {
        log.warn("DatastoreException thrown:", ex);
        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(SERVICE_UNAVAILABLE, ex.getMessage()));
    }

    // Unhandled cases
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleOtherExceptions(Exception ex) {
        log.error("Exception thrown:", ex);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody(INTERNAL_SERVER_ERROR, ex.getMessage()));
    }

    private static String constructValidationExceptionMsg(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String reason = "Reason: ";

        if (!CollectionUtils.isEmpty(fieldErrors)) {
            reason += fieldErrors.stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }

        return "Request validation failed. " + reason;
    }

    private static ResponseDTO responseBody(HttpStatus httpStatus, String exMessage) {
        return new ResponseDTO(httpStatus.value(), exMessage);
    }
}