package com.example.splitwise.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The console version never validated input beyond a Python exception
 * crashing the program on bad input (e.g. float("abc")). Here, invalid
 * JSON bodies (missing payer, empty participants list, negative amount,
 * etc.) are caught and turned into a clean 400 response instead of a
 * stack trace - the equivalent of gracefully handling bad `input()` values.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleUnknownUser(NullPointerException ex) {
        // Thrown e.g. if simplifyDebts/getBalances references a user that was
        // never added via /api/members first.
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", "Referenced a member that hasn't been added yet. Add members via POST /api/members first.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
