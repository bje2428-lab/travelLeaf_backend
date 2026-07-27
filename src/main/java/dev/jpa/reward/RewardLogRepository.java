package dev.jpa.reward;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RewardLogRepository
        extends JpaRepository<RewardLog, Long> {

    boolean existsByUserNoAndSourceTypeAndSourceKey(
            Long userNo,
            String sourceType,
            String sourceKey
    );

    // ✅ 추가: 내 보상 기록 조회
    List<RewardLog> findByUserNoOrderByCreatedAtDesc(Long userNo);
}
