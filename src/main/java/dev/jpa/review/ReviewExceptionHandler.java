package dev.jpa.review;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ReviewExceptionHandler {

    @ExceptionHandler(ReviewBlockedException.class)
    public ResponseEntity<?> handleBlocked(ReviewBlockedException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "toxicScore", e.getToxicScore(),
                "sentiment", e.getSentiment(),
                "flagReason", e.getFlagReason()
        ));
    }
}
