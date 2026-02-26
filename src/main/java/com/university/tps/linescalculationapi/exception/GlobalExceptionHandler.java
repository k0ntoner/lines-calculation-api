package com.university.tps.linescalculationapi.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =============================
    // @Valid body validation
    // =============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException exception) {

        List<ViolationDTO> fieldViolations =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::mapFieldError)
                        .toList();

        List<ViolationDTO> globalViolations =
                exception.getBindingResult()
                        .getGlobalErrors()
                        .stream()
                        .map(error -> ViolationDTO.builder()
                                .path("lines")
                                .message(error.getDefaultMessage())
                                .build())
                        .toList();

        List<ViolationDTO> all = new ArrayList<>();
        all.addAll(fieldViolations);
        all.addAll(globalViolations);

        ErrorResponse response = ErrorResponse.builder()
                .message("Validation failed")
                .violations(all.isEmpty() ? null : all)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =============================
    // constraint validation
    // =============================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintValidation(
            ConstraintViolationException exception
    ) {

        List<ViolationDTO> violations =
                exception.getConstraintViolations()
                        .stream()
                        .map(v -> ViolationDTO.builder()
                                .path(v.getPropertyPath().toString())
                                .message(v.getMessage())
                                .build())
                        .toList();

        ErrorResponse response = ErrorResponse.builder()
                .message("Validation failed")
                .violations(violations)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // =============================
    // IllegalArgumentException
    // =============================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception
    ) {

        ErrorResponse response = ErrorResponse.builder()
                .message(exception.getMessage())
                .violations(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // =============================
    // IllegalStateException
    // =============================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException exception
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .message(exception.getMessage())
                .violations(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // =============================
    // fallback
    // =============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception
    ) {

        log.error(exception.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .message("Unexpected server error")
                .violations(List.of())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ViolationDTO mapFieldError(FieldError error) {

        return ViolationDTO.builder()
                .path(error.getField())
                .message(error.getDefaultMessage())
                .build();
    }
}