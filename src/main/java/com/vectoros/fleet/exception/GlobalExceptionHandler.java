package com.vectoros.fleet.exception;

import com.vectoros.fleet.dto.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RobotNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRobotNotFound(RobotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(DuplicateRobotException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateRobot(DuplicateRobotException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskNotFound(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(InvalidTaskStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTaskState(InvalidTaskStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(TaskAssignmentException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskAssignment(TaskAssignmentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(RobotUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRobotUnavailable(RobotUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse.of(ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> errors = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid value" : fe.getDefaultMessage()))
                .toList();

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.unprocessableEntity().body(ApiErrorResponse.of("Validation failed.", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .toList();

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.unprocessableEntity().body(ApiErrorResponse.of("Validation failed.", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("Internal server error.", List.of()));
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
        return path.isBlank() ? violation.getMessage() : path + ": " + violation.getMessage();
    }
}

