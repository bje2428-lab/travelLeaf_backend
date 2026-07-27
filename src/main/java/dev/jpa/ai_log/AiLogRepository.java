package dev.jpa.ai_log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiLogRepository extends JpaRepository<AiLog, Long> {

    /* =========================
       페이지네이션 (전체 로그)
    ========================= */
    Page<AiLog> findAllByOrderByLogIdDesc(Pageable pageable);

    /* =========================
       페이지네이션 + 상태 필터
       status = SUCCESS / FAIL
    ========================= */
    Page<AiLog> findByStatusOrderByLogIdDesc(String status, Pageable pageable);
}
