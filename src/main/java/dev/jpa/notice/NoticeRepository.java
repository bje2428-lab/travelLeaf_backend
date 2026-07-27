package dev.jpa.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // =====================================================
    // ❌ 기존 메서드들 (유지)
    // =====================================================

    // 검색 (페이징 없음)
    List<Notice> findByTitleContainingOrContentContaining(
            String keyword1,
            String keyword2
    );

    // 카테고리별 조회 (페이징 없음)
    List<Notice> findByCategoryOrderByCreatedAtDesc(String category);

    // 상단 고정 조회 (페이징 없음)
    List<Notice> findByIsFixedOrderByCreatedAtDesc(String isFixed);

    // =====================================================
    // ✅ 페이징용 메서드들 (추가)
    // =====================================================

    // 🔥 검색 + 페이징
    Page<Notice> findByTitleContainingOrContentContaining(
            String keyword1,
            String keyword2,
            Pageable pageable
    );

    // 🔥 카테고리 + 페이징
    Page<Notice> findByCategory(
            String category,
            Pageable pageable
    );

    // 🔥 상단 고정 공지 + 페이징 (선택)
    Page<Notice> findByIsFixed(
            String isFixed,
            Pageable pageable
    );
}
