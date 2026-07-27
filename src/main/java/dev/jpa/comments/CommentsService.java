package dev.jpa.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import dev.jpa.ai.moderation.ModerationService;   // 🔥 추가

@Service
public class CommentsService {

    public CommentsService() {
        System.out.println("-> CommentsService created.");
    }

    @Autowired
    private CommentsRepository commentsRepository;

    // 🔥 LLM 악성도 분석 서비스
    @Autowired
    private ModerationService moderationService;

    /* ===============================
       댓글 작성 (세션 기반)
    =============================== */
    @Transactional
    public Comments create(String userId, CommentsDTO dto) {
        if (userId == null) {
            throw new SecurityException("로그인 필요");
        }

        Comments c = new Comments();
        c.setUserId(userId);
        c.setPostId(dto.getPostId());
        c.setParentCommentId(dto.getParentCommentId());
        c.setContent(dto.getContent());
        c.setImageUrl(dto.getImageUrl());
        c.setIsDeleted("N");

        // 🔥 댓글 저장
        Comments saved = commentsRepository.save(c);

        // 🔥 LLM 악성도 분석 (비동기 / 실패해도 댓글 등록 유지)
        try {
            moderationService.analyzeComment(
                saved.getCommentId(),
                saved.getContent()
            );
        } catch (Exception e) {
            System.out.println("⚠ AI 댓글 분석 실패 (댓글 등록은 유지) commentId=" + saved.getCommentId());
            e.printStackTrace();
        }

        return saved;
    }

    /* ===============================
       기존 댓글 목록 (유지)
    =============================== */
    public List<Comments> listByPost(Long postId) {
        return commentsRepository.findByPostIdAndIsDeletedOrderByCommentIdAsc(postId, "N");
    }

    /* ===============================
       기본 트리 구조 (기존)
    =============================== */
    public List<Comments> listTreeByPost(Long postId) {
        return commentsRepository.findTreeByPostId(postId);
    }

    /* =========================================================
       ⭐ 트리 + 정렬 옵션 지원
       sort = oldest | latest | likes
    ========================================================= */
    public List<Comments> listTreeByPost(Long postId, String sort) {

        if ("likes".equals(sort)) {

            List<Comments> parents = commentsRepository.findByPostIdOrderByLikesDesc(postId)
                    .stream()
                    .filter(c -> c.getParentCommentId() == null)
                    .collect(Collectors.toList());

            List<Comments> all = commentsRepository.findByPostIdAndIsDeleted(
                    postId,
                    "N",
                    Sort.by(
                            Sort.Order.asc("parentCommentId"),
                            Sort.Order.asc("commentId")
                    )
            );

            Map<Long, List<Comments>> replyMap = new HashMap<>();
            for (Comments c : all) {
                if (c.getParentCommentId() != null) {
                    replyMap.computeIfAbsent(c.getParentCommentId(), k -> new ArrayList<>()).add(c);
                }
            }

            List<Comments> result = new ArrayList<>();
            for (Comments parent : parents) {
                result.add(parent);
                List<Comments> replies = replyMap.get(parent.getCommentId());
                if (replies != null) result.addAll(replies);
            }

            return result;
        }

        Sort s;
        if ("latest".equals(sort)) {
            s = Sort.by(
                    Sort.Order.asc("parentCommentId"),
                    Sort.Order.desc("commentId")
            );
        } else {
            s = Sort.by(
                    Sort.Order.asc("parentCommentId"),
                    Sort.Order.asc("commentId")
            );
        }

        return commentsRepository.findByPostIdAndIsDeleted(postId, "N", s);
    }

    /* ===============================
       페이징 (기존 유지)
    =============================== */
    public Page<Comments> listByPostPaging(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("parentCommentId"),
                        Sort.Order.asc("commentId")
                )
        );

        return commentsRepository.findByPostIdAndIsDeleted(postId, "N", pageable);
    }

    /* ===============================
       댓글 수
    =============================== */
    public long countByPost(Long postId) {
        return commentsRepository.countByPostIdAndIsDeleted(postId, "N");
    }

    /* ===============================
       댓글 수정 (세션 기반)
    =============================== */
    @Transactional
    public Comments update(Long commentId, String userId, CommentsDTO dto) {
        if (userId == null) throw new SecurityException("로그인 필요");

        Comments c = Optional.ofNullable(
                commentsRepository.findByCommentIdAndIsDeleted(commentId, "N")
        ).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!c.getUserId().equals(userId)) throw new SecurityException("작성자만 수정 가능");

        c.setContent(dto.getContent());
        c.setImageUrl(dto.getImageUrl());
        return commentsRepository.save(c);
    }

    /* ===============================
       사용자 삭제
    =============================== */
    @Transactional
    public void delete(Long commentId, String userId) {
        if (userId == null) throw new SecurityException("로그인 필요");

        Comments c = Optional.ofNullable(
                commentsRepository.findByCommentIdAndIsDeleted(commentId, "N")
        ).orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

        if (!c.getUserId().equals(userId)) throw new SecurityException("삭제 권한 없음");

        commentsRepository.softDeleteById(commentId);

        if (c.getParentCommentId() == null) {
            commentsRepository.softDeleteRepliesByParentCommentId(commentId);
        }
    }

    /* ===============================
       관리자 삭제
    =============================== */
    @Transactional
    public void adminDelete(Long commentId, String requestUserId, int grade) {
        if (grade != 2) throw new SecurityException("관리자만 가능");

        Comments c = commentsRepository.findByCommentIdAndIsDeleted(commentId, "N");
        if (c == null) return;

        commentsRepository.softDeleteById(commentId);

        if (c.getParentCommentId() == null) {
            commentsRepository.softDeleteRepliesByParentCommentId(commentId);
        }
    }

    /* ===============================
       관리자: 게시글 전체 삭제
    =============================== */
    @Transactional
    public void adminDeleteAllCommentsByPost(Long postId, String requestUserId, int grade) {
        if (grade != 2) throw new SecurityException("관리자만 가능");
        commentsRepository.softDeleteAllByPostId(postId);
    }
    
 // 🔥 부모 댓글 + 대댓글 ID 목록 조회 (신고 정리용)
    public List<Long> findThreadCommentIds(Long rootId) {
        return commentsRepository.findThreadCommentIds(rootId);
    }

}
