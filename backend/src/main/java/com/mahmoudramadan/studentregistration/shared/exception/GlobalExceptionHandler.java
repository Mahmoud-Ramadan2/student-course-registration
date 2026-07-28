package com.mahmoudramadan.studentregistration.shared.exception;

import com.mahmoudramadan.studentregistration.shared.dto.ApiResponse;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

    @RestControllerAdvice
    @Slf4j
    public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        // =========================================================================
        // Spring MVC Exceptions
        // =========================================================================

        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                MethodArgumentNotValidException ex,
                HttpHeaders headers,
                HttpStatusCode status,
                WebRequest request) {

            Map<String, String> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            field -> field.getDefaultMessage() != null
                                    ? field.getDefaultMessage()
                                    : "Invalid value",
                            (first, second) -> first
                    ));

            ApiResponse<Map<String, String>> response =
                    ApiResponse.<Map<String, String>>builder()
                            .success(false)
                            .message("Validation failed")
                            .data(errors)
                            .timestamp(Instant.now())
                            .requestId(MDC.get("requestId"))
                            .build();

            return ResponseEntity.badRequest().body(response);
        }


        @Override
        protected ResponseEntity<Object> handleMissingServletRequestParameter(
                MissingServletRequestParameterException ex,
                HttpHeaders headers,
                HttpStatusCode status,
                WebRequest request) {

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Missing required request parameter: " + ex.getParameterName()));
        }

        @Override
        protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
                HttpRequestMethodNotSupportedException ex,
                HttpHeaders headers,
                HttpStatusCode status,
                WebRequest request) {

            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(ApiResponse.error("HTTP method not supported"));
        }

        @Override
        protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
                HttpMediaTypeNotSupportedException ex,
                HttpHeaders headers,
                HttpStatusCode status,
                WebRequest request) {

            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(ApiResponse.error("Unsupported content type"));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
                MethodArgumentTypeMismatchException ex) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid value for parameter: " + ex.getName());
        }

        // =========================================================================
        // Business Exceptions
        // =========================================================================

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
                ResourceNotFoundException ex) {

            return error(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(
                BusinessException ex) {

            return error(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        // =========================================================================
        // Security Exceptions
        // =========================================================================

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
                AccessDeniedException ex) {

            return error(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this action");
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                BadCredentialsException ex) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password");
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(
                DisabledException ex) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Please verify your email before logging in");
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ApiResponse<Void>> handleLockedAccount(
                LockedException ex) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Account is locked. Contact support");
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ApiResponse<Void>> handleJwtException(
                JwtException ex) {

            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token");
        }

        // =========================================================================
        // Fallback Exception
        // =========================================================================

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleUnhandledException(
                Exception ex) {

            log.error("Unhandled exception", ex);

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred");
        }

        // =========================================================================
        // Helper Methods
        // =========================================================================

        private ResponseEntity<ApiResponse<Void>> error(
                HttpStatus status,
                String message) {

            return ResponseEntity.status(status)
                    .body(ApiResponse.error(message));
        }

    }