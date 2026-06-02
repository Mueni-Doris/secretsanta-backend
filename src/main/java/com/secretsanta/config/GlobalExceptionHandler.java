package com.secretsanta.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception exception, HttpServletRequest request) {
        log.error(
                "unhandled_error method={} path={} remoteAddr={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                exception
        );

        return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
    }
}
