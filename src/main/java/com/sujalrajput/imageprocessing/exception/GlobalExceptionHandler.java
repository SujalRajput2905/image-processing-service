    package com.sujalrajput.imageprocessing.exception;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.FieldError;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
    import org.springframework.web.multipart.MaxUploadSizeExceededException;

    import java.time.Instant;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.stream.Collectors;

    @RestControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(UserAlreadyExistsException.class)
        public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(
                UserAlreadyExistsException ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.CONFLICT.value());
            error.put("error", "Conflict");
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(error);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<Map<String, Object>> handleAuthentication(
                AuthenticationException ex) {

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.UNAUTHORIZED.value());
            error.put("error", "Unauthorized");
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        @ExceptionHandler(FileUploadException.class)
        public ResponseEntity<Map<String, Object>> handleFileUploadException(FileUploadException ex) {
            Map<String, Object> error = new HashMap<>();

            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Bad Request");
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGenericException(
                Exception ex) {
            ex.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("error", "Internal Server Error");
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }

        @ExceptionHandler(ImageNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleImageNotFound(ImageNotFoundException ex) {
            ex.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.NOT_FOUND.value());
            error.put("error", "Not Found");
            error.put("message", ex.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
            Map<String, Object> error = new HashMap<>();

            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Bad Request");
            error.put("message", "File size exceeds maximum upload limit of 15MB");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(
                MethodArgumentNotValidException ex) {

            Map<String, String> fieldErrors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Validation Failed");
            error.put("messages", fieldErrors);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }
