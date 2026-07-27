package dev.jpa.review;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice(basePackages = "dev.jpa.review")
public class ReviewReportErrorAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleRSE(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "error", "ResponseStatusException",
                "status", e.getStatusCode().value(),
                "message", e.getReason() == null ? e.getMessage() : e.getReason()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDIV(DataIntegrityViolationException e) {
        Throwable root = root(e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "DataIntegrityViolationException",
                "message", root.getMessage()
        ));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSQL(SQLException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "SQLException",
                "sqlState", e.getSQLState(),
                "code", e.getErrorCode(),
                "message", e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception e) {
        Throwable root = root(e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getClass().getName(),
                "message", root.getMessage()
        ));
    }

    private static Throwable root(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}
