package com.undoschool.booking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        return ResponseEntity.status(mapStatus(ex.getErrorCode()))
                .body(buildResponse(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(buildResponse(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(ErrorCode.UNAUTHORIZED, "Invalid credentials"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(ErrorCode.UNAUTHORIZED, "Authentication required"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildResponse(ErrorCode.FORBIDDEN, "Access denied"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        if (isExclusionViolation(ex)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(buildResponse(ErrorCode.TIME_CONFLICT,
                            "Booking overlaps with an existing session"));
        }
        if (isDuplicateBooking(ex)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(buildResponse(ErrorCode.ALREADY_BOOKED,
                            "Offering is already booked"));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildResponse(ErrorCode.VALIDATION_ERROR, "Data constraint violation"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse(ErrorCode.INTERNAL_ERROR, "Unexpected server error"));
    }

    private boolean isExclusionViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            return "23P01".equals(cve.getSQLState());
        }
        return ex.getMessage() != null && ex.getMessage().contains("no_parent_time_overlap");
    }

    private boolean isDuplicateBooking(DataIntegrityViolationException ex) {
        return ex.getMessage() != null && ex.getMessage().contains("uq_bookings_parent_offering_confirmed");
    }

    private HttpStatus mapStatus(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TIME_CONFLICT, ALREADY_BOOKED -> HttpStatus.CONFLICT;
            case OFFERING_NOT_BOOKABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case PROFILE_REQUIRED -> HttpStatus.FORBIDDEN;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ErrorResponse buildResponse(ErrorCode code, String message) {
        return ErrorResponse.of(code, message);
    }
}
