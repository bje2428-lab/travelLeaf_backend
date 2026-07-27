package dev.jpa.routing.controller;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Order(0) // 다른 Advice가 있으면 우선 적용(선택)
public class RouteErrorAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(409, e.getMessage()));
    }

    // ✅ 핵심: ResponseStatusException(403/404 등) status 유지
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        String msg = (e.getReason() != null ? e.getReason() : e.getMessage());
        return ResponseEntity.status(status).body(errorBody(status, msg));
    }

    // ✅ @ResponseStatus 붙은 커스텀 예외도 status 유지
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusAnnotation(RuntimeException e) {
        ResponseStatus rs = e.getClass().getAnnotation(ResponseStatus.class);
        if (rs != null) {
            int status = rs.value().value();
            return ResponseEntity.status(status).body(errorBody(status, e.getMessage()));
        }
        // @ResponseStatus 없으면 여기서 그냥 던져서 아래 serverError로 가게
        throw e;
    }

    // ✅ 진짜 서버 에러만 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> serverError(Exception e) {
        e.printStackTrace(); // 콘솔에 원인 찍기(중요)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(500, "서버 오류(콘솔 로그 확인)"));
    }

    private Map<String, Object> errorBody(int status, String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", LocalDateTime.now().toString());
        m.put("status", status);
        m.put("error", msg);
        return m;
    }
}
