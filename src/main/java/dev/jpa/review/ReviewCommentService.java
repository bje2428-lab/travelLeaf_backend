package dev.jpa.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReviewCommentService {

    @Autowired private ReviewRepository reviewRepo;
    @Autowired private ReviewCommentRepository commentRepo;
    @Autowired private ReviewCommentLikeRepository likeRepo;

    /* =========================
       댓글 작성
    ========================= */
    @Transactional
    public ReviewCommentResponseDTO create(Long reviewId, ReviewCommentCreateDTO dto) {
        // 리뷰 존재 체크
        reviewRepo.findById(reviewId).orElseThrow(() -> new RuntimeException("리뷰 없음"));

        String userId = safe(dto.getUserId());
        String content = safe(dto.getContent());

        if (userId.isBlank()) throw new RuntimeException("userId가 필요합니다.");
        if (content.isBlank()) throw new RuntimeException("댓글 내용이 비어있습니다.");

        ReviewComment c = new ReviewComment();
        c.setReviewId(reviewId);
        c.setUserId(userId);
        c.setContent(content);
        c.setIsDeleted(0);

        ReviewComment saved = commentRepo.save(c);

        ReviewCommentResponseDTO out = toDto(saved);
        out.setLikeCount(0);
        out.setLikedByMe(false);
        return out;
    }

    /* =========================
       댓글 목록 (좋아요수 + 내가 눌렀는지)
       - userId 없으면 likedByMe=false로만 내려줌
    ========================= */
    @Transactional(readOnly = true)
    public Page<ReviewCommentResponseDTO> list(Long reviewId, String userId, int page, int size) {
        reviewRepo.findById(reviewId).orElseThrow(() -> new RuntimeException("리뷰 없음"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ReviewComment> p = commentRepo.findByReviewIdOrderByCreatedAtAsc(reviewId, pageable);

        List<ReviewComment> comments = p.getContent();
        List<Long> ids = new ArrayList<>();
        for (ReviewComment c : comments) ids.add(c.getCommentId());

        // 좋아요 여부(내가 눌렀는지)
        Set<Long> likedSet = new HashSet<>();
        String uid = safe(userId);
        if (!uid.isBlank() && !ids.isEmpty()) {
            likedSet.addAll(likeRepo.findLikedCommentIds(uid, ids));
        }

        // 좋아요 수
        List<ReviewCommentResponseDTO> dtoList = new ArrayList<>();
        for (ReviewComment c : comments) {
            ReviewCommentResponseDTO dto = toDto(c);

            long likeCount = likeRepo.countByCommentId(c.getCommentId());
            dto.setLikeCount(likeCount);

            boolean likedByMe = (!uid.isBlank() && likedSet.contains(c.getCommentId()));
            dto.setLikedByMe(likedByMe);

            // 소프트 삭제된 댓글이면 내용 가림 처리
            if (c.getIsDeleted() != null && c.getIsDeleted() == 1) {
                dto.setContent("삭제된 댓글입니다");
            }

            dtoList.add(dto);
        }

        return new PageImpl<>(dtoList, pageable, p.getTotalElements());
    }

    /* =========================
       댓글 삭제 (작성자만)
       - soft delete
    ========================= */
    @Transactional
    public void delete(Long commentId, String userId) {
        ReviewComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        String uid = safe(userId);
        if (uid.isBlank()) throw new RuntimeException("userId가 필요합니다.");

        if (!uid.equals(safe(c.getUserId()))) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        if (c.getIsDeleted() != null && c.getIsDeleted() == 1) {
            return; // 이미 삭제됨
        }

        c.setIsDeleted(1);
        c.setDeletedAt(java.time.LocalDateTime.now());
        commentRepo.save(c);

        // (선택) 좋아요는 남겨도 되지만 보통 같이 지움
        likeRepo.deleteByCommentIdHard(commentId);
    }

    /* =========================
       ✅ 관리자 강제 삭제
       - 작성자 체크 없이 삭제 가능 (신고 승인/관리자 처리용)
       - 너 기존 스타일 유지: soft delete + 좋아요 hard delete
       - 필요하면 managerId로 관리자 검증(User 테이블 grade 확인)도 여기서 추가 가능
    ========================= */
    @Transactional
    public void forceDeleteByManager(Long commentId, String managerId) {
        ReviewComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        String mid = safe(managerId);
        if (mid.isBlank()) throw new RuntimeException("managerId가 필요합니다.");

        // ✅ 이미 삭제된 경우는 그냥 종료
        if (c.getIsDeleted() != null && c.getIsDeleted() == 1) {
            return;
        }

        // ✅ 작성자 체크 없음 (핵심)
        c.setIsDeleted(1);
        c.setDeletedAt(java.time.LocalDateTime.now());
        commentRepo.save(c);

        // ✅ 좋아요는 같이 제거
        likeRepo.deleteByCommentIdHard(commentId);
    }

    /* =========================
       댓글 좋아요 토글
    ========================= */
    @Transactional
    public ToggleLikeResponseDTO toggleLike(Long commentId, String userId) {
        ReviewComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        // 삭제된 댓글에는 좋아요 못하게
        if (c.getIsDeleted() != null && c.getIsDeleted() == 1) {
            throw new RuntimeException("삭제된 댓글에는 좋아요를 할 수 없습니다.");
        }

        String uid = safe(userId);
        if (uid.isBlank()) throw new RuntimeException("userId가 필요합니다.");

        boolean exists = likeRepo.existsByCommentIdAndUserId(commentId, uid);
        boolean likedAfter;

        if (exists) {
            likeRepo.deleteByCommentIdAndUserId(commentId, uid);
            likedAfter = false;
        } else {
            ReviewCommentLike l = new ReviewCommentLike();
            l.setCommentId(commentId);
            l.setUserId(uid);
            try {
                likeRepo.save(l);
                likedAfter = true;
            } catch (DataIntegrityViolationException e) {
                // 동시성으로 중복 insert가 날아온 경우 등
                likedAfter = true;
            }
        }

        long likeCount = likeRepo.countByCommentId(commentId);

        ToggleLikeResponseDTO out = new ToggleLikeResponseDTO();
        out.setLiked(likedAfter);
        out.setLikeCount(likeCount);
        return out;
    }

    private static ReviewCommentResponseDTO toDto(ReviewComment c) {
        ReviewCommentResponseDTO dto = new ReviewCommentResponseDTO();
        dto.setCommentId(c.getCommentId());
        dto.setReviewId(c.getReviewId());
        dto.setUserId(c.getUserId());
        dto.setContent(c.getContent());
        dto.setIsDeleted(c.getIsDeleted());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
