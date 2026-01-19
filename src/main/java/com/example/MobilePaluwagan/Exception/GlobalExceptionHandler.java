package com.example.MobilePaluwagan.Exception;

import com.example.MobilePaluwagan.DTOs.Response.ApiResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                ex.getReason(),
                null
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
}
