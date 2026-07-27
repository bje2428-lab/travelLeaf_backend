package dev.jpa.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {

    @Autowired private ReviewRepository repo;
    @Autowired private ReviewModerationService moderationService; // 기존
    @Autowired private ReviewAiService aiService;                 // ✅ AI 요약/태깅 + 번역
    @Autowired private ReviewTagRepository tagRepo;               // ✅ 태그 저장소

    // ✅ 댓글 기능 추가로 주입
    @Autowired private ReviewCommentRepository commentRepo;
    @Autowired private ReviewCommentLikeRepository commentLikeRepo;

    // ✅ 자동 태깅을 "등록/수정 시" 할지 여부 (원하면 true로)
    private static final boolean AUTO_TAG_ON_SAVE = false;

    /* ============================
       ⭐ CREATE (moderation 유지)
    ============================ */
    @Transactional
    public Review create(ReviewDTO dto) {

        String checkText = safe(dto.getPlaceName()) + " " + safe(dto.getContent());
        AiReviewCheck chk = moderationService.check(checkText);

        if (!chk.allowed) {
            String msg = "부적절한 표현이 포함되어 있어 등록할 수 없습니다. 표현을 순화해 주세요.";
            if ("SWEAR".equals(chk.flagReason)) {
                msg = "욕설이 포함되어 있어 등록할 수 없습니다. 욕설을 제거해 주세요.";
            }
            throw new ReviewBlockedException(msg, chk.toxicScore, chk.sentiment, chk.flagReason);
        }

        Review r = new Review();
        r.setCity(safe(dto.getCity()));
        r.setDistrict(safe(dto.getDistrict()));
        r.setPlaceName(safe(dto.getPlaceName()));
        r.setUserId(safe(dto.getUserId()));
        r.setRating(dto.getRating());
        r.setContent(safe(dto.getContent()));

        // ✅ 독성/감정 저장
        r.setToxicScore(chk.toxicScore);
        r.setFlagReason(chk.flagReason);
        r.setSentiment(chk.sentiment);

        // ✅ LocalDateTime 필드라서 now() 사용
        r.setModeratedAt(LocalDateTime.now());

        Review saved = repo.save(r);

        // ✅ (선택) 자동 태깅
        if (AUTO_TAG_ON_SAVE) {
            regenerateTags(saved.getReviewId());
        }

        return saved;
    }

    /* ============================
       ⭐ LIST
    ============================ */
    @Transactional(readOnly = true)
    public List<Review> list(String city, String district) {
        return repo.findByCityAndDistrictNative(safe(city), safe(district));
    }

    /* ============================
       ✅ SEARCH + SORT + PAGING
    ============================ */
    @Transactional(readOnly = true)
    public Page<Review> search(
            String city,
            String district,
            String keyword,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        city = safe(city);
        district = safe(district);
        keyword = (keyword == null) ? null : keyword.trim();

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        sortBy = normalizeSortBy(sortBy);
        direction = normalizeDirection(direction);

        Pageable pageable = PageRequest.of(safePage, safeSize);

        return repo.searchByCityDistrictWithPaging(
                city, district, keyword, sortBy, direction, pageable
        );
    }

    private static final Set<String> ALLOWED_SORT = Set.of("createdAt", "rating");

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null) return "createdAt";
        String s = sortBy.trim();

        if ("created_at".equalsIgnoreCase(s) || "createdAt".equalsIgnoreCase(s)) return "createdAt";
        if ("rating".equalsIgnoreCase(s)) return "rating";

        return ALLOWED_SORT.contains(s) ? s : "createdAt";
    }

    private String normalizeDirection(String direction) {
        if (direction == null) return "desc";
        String d = direction.trim().toLowerCase();
        return (d.equals("asc") || d.equals("desc")) ? d : "desc";
    }

    /* ============================
       ⭐ DETAIL
    ============================ */
    @Transactional(readOnly = true)
    public Review detail(Long id) {
        return repo.findById(id).orElse(null);
    }

    /* =========================================================
       ✅ DETAIL + COMMENTS (한번에 내려주는 DTO)
       - viewerUserId: "내가 좋아요 눌렀는지" 계산용 (없으면 null/"" 가능)
       - 댓글 페이징 포함
       ========================================================= */
    @Transactional(readOnly = true)
    public ReviewDetailResponseDTO detailWithComments(
            Long reviewId,
            String viewerUserId,
            int commentPage,
            int commentSize
    ) {
        Review r = repo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        // 태그도 같이 포함(원치 않으면 삭제 가능)
        List<ReviewTag> tags = tagRepo.findByReviewId(reviewId);

        // 댓글 페이지 로딩
        int safePage = Math.max(commentPage, 0);
        int safeSize = Math.max(1, Math.min(commentSize, 100));

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("createdAt").ascending()
        );

        Page<ReviewComment> cpage = commentRepo.findByReviewIdOrderByCreatedAtAsc(reviewId, pageable);

        List<ReviewComment> comments = cpage.getContent();
        List<Long> commentIds = new ArrayList<>();
        for (ReviewComment c : comments) commentIds.add(c.getCommentId());

        String uid = safe(viewerUserId);

        // 1) 내가 좋아요 눌렀는지
        Set<Long> likedSet = new HashSet<>();
        if (!uid.isBlank() && !commentIds.isEmpty()) {
            likedSet.addAll(commentLikeRepo.findLikedCommentIds(uid, commentIds));
        }

        // 2) 좋아요 수를 한번에 group-by로 가져오기
        Map<Long, Long> likeCountMap = new HashMap<>();
        if (!commentIds.isEmpty()) {
            List<Object[]> rows = commentLikeRepo.countLikesByCommentIds(commentIds);
            for (Object[] row : rows) {
                Long cid = ((Number) row[0]).longValue();
                Long cnt = ((Number) row[1]).longValue();
                likeCountMap.put(cid, cnt);
            }
        }

        // 3) Comment DTO 변환
        List<ReviewCommentResponseDTO> commentDtos = new ArrayList<>();
        for (ReviewComment c : comments) {
            ReviewCommentResponseDTO dto = new ReviewCommentResponseDTO();
            dto.setCommentId(c.getCommentId());
            dto.setReviewId(c.getReviewId());
            dto.setUserId(c.getUserId());
            dto.setIsDeleted(c.getIsDeleted());
            dto.setCreatedAt(c.getCreatedAt());

            if (c.getIsDeleted() != null && c.getIsDeleted() == 1) {
                dto.setContent("삭제된 댓글입니다");
            } else {
                dto.setContent(c.getContent());
            }

            long likeCount = likeCountMap.getOrDefault(c.getCommentId(), 0L);
            dto.setLikeCount(likeCount);

            boolean likedByMe = (!uid.isBlank() && likedSet.contains(c.getCommentId()));
            dto.setLikedByMe(likedByMe);

            commentDtos.add(dto);
        }

        // ===== 최종 응답 DTO 조립 =====
        ReviewDetailResponseDTO out = new ReviewDetailResponseDTO();

        out.setReviewId(r.getReviewId());
        out.setCity(r.getCity());
        out.setDistrict(r.getDistrict());
        out.setPlaceName(r.getPlaceName());
        out.setUserId(r.getUserId());
        out.setRating(r.getRating());
        out.setContent(r.getContent());

        out.setCreatedAt(r.getCreatedAt());
        out.setUpdatedAt(r.getUpdatedAt());

        out.setToxicScore(r.getToxicScore());
        out.setFlagReason(r.getFlagReason());
        out.setSentiment(r.getSentiment());

        out.setModeratedAt(r.getModeratedAt());
        out.setAiSummary(r.getAiSummary());
        out.setAiKeywords(r.getAiKeywords());
        out.setAiSummaryAt(r.getAiSummaryAt());

        out.setTags(tags);
        out.setComments(commentDtos);

        out.setCommentPage(safePage);
        out.setCommentSize(safeSize);
        out.setCommentTotalElements(cpage.getTotalElements());
        out.setCommentTotalPages(cpage.getTotalPages());

        return out;
    }

    /* ============================
       ⭐ UPDATE (moderation 유지)
    ============================ */
    @Transactional
    public Review update(Long reviewId, ReviewDTO dto) {
        Review origin = repo.findByReviewIdAndUserId(reviewId, dto.getUserId());
        if (origin == null) {
            throw new RuntimeException("리뷰를 수정할 권한이 없습니다.");
        }

        String checkText = safe(origin.getPlaceName()) + " " + safe(dto.getContent());
        AiReviewCheck chk = moderationService.check(checkText);

        if (!chk.allowed) {
            String msg = "부적절한 표현이 포함되어 있어 수정할 수 없습니다. 표현을 순화해 주세요.";
            if ("SWEAR".equals(chk.flagReason)) {
                msg = "욕설이 포함되어 있어 수정할 수 없습니다. 욕설을 제거해 주세요.";
            }
            throw new ReviewBlockedException(msg, chk.toxicScore, chk.sentiment, chk.flagReason);
        }

        origin.setRating(dto.getRating());
        origin.setContent(safe(dto.getContent()));

        // updatedAt은 Date면 Date 유지
        origin.setUpdatedAt(new Date());

        // ✅ 독성/감정 저장
        origin.setToxicScore(chk.toxicScore);
        origin.setFlagReason(chk.flagReason);
        origin.setSentiment(chk.sentiment);

        // ✅ LocalDateTime 필드라서 now() 사용
        origin.setModeratedAt(LocalDateTime.now());

        Review saved = repo.save(origin);

        // ✅ (선택) 자동 태깅
        if (AUTO_TAG_ON_SAVE) {
            regenerateTags(saved.getReviewId());
        }

        return saved;
    }

    /* ============================
       ⭐ DELETE
       - ✅ 리뷰 삭제 시 댓글/댓글좋아요/태그 같이 정리
    ============================ */
    @Transactional
    public void delete(Long reviewId, String userId) {
        Review r = repo.findByReviewIdAndUserId(reviewId, userId);
        if (r == null) throw new RuntimeException("삭제 권한이 없습니다.");

        // ✅ 1) 댓글 좋아요 먼저 삭제 (FK 걸어놨으면 순서 중요)
        commentLikeRepo.deleteByReviewIdHard(reviewId);

        // ✅ 2) 댓글 삭제
        commentRepo.deleteByReviewIdHard(reviewId);

        // ✅ 3) 태그 삭제
        tagRepo.deleteByReviewId(reviewId);

        // ✅ 4) 리뷰 삭제
        repo.delete(r);
    }

    /* =========================================================
       ✅ 1) 요약/키워드 생성 (버튼 클릭용)
       - 요약/키워드 REVIEW에 저장
       - 태그도 같이 저장
       ========================================================= */
    @Transactional
    public AiReviewSummaryResponse generateSummary(Long reviewId) {
        Review r = repo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        AiReviewSummaryResponse ai = aiService.summarizeAndTag(r.getPlaceName(), r.getContent());

        r.setAiSummary(ai.summary1);
        r.setAiKeywords((ai.keywords == null) ? "" : String.join(",", ai.keywords));

        // ✅ LocalDateTime 필드라서 now() 사용
        r.setAiSummaryAt(LocalDateTime.now());

        repo.save(r);

        // ✅ 태그 DB 저장 (기존 태그 삭제 후 새로 저장)
        tagRepo.deleteByReviewId(reviewId);

        if (ai.tags != null) {
            for (Map.Entry<String, String> e : ai.tags.entrySet()) {
                String type = safe(e.getKey());
                String value = safe(e.getValue());
                if (type.isBlank() || value.isBlank()) continue;

                tagRepo.save(new ReviewTag(reviewId, type, value));
            }
        }

        return ai;
    }

    /* =========================================================
       ✅ 2) 번역 기능 (저장 X, 화면에만)
       ========================================================= */
    @Transactional(readOnly = true)
    public ReviewAiService.TranslateReviewResponse translateReview(Long reviewId, String targetLang) {
        Review r = repo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰 없음"));

        return aiService.translateReview(r.getPlaceName(), r.getContent(), targetLang);
    }

    /* =========================================================
       ✅ 3) 태그 재생성 (버튼 클릭 또는 자동 태깅용)
       ========================================================= */
    @Transactional
    public List<ReviewTag> regenerateTags(Long reviewId) {
        Review r = repo.findById(reviewId).orElseThrow(() -> new RuntimeException("리뷰 없음"));
        AiReviewSummaryResponse ai = aiService.summarizeAndTag(r.getPlaceName(), r.getContent());
        return saveTags(reviewId, ai);
    }

    /* =========================================================
       ✅ 태그 조회
       ========================================================= */
    @Transactional(readOnly = true)
    public List<ReviewTag> getTags(Long reviewId) {
        return tagRepo.findByReviewId(reviewId);
    }

    /* =========================================================
       ✅ 공통: 태그 저장 로직
       ========================================================= */
    private List<ReviewTag> saveTags(Long reviewId, AiReviewSummaryResponse ai) {
        tagRepo.deleteByReviewId(reviewId);

        List<ReviewTag> saved = new ArrayList<>();
        if (ai == null || ai.tags == null) return saved;

        for (Map.Entry<String, String> e : ai.tags.entrySet()) {
            String type = safe(e.getKey());
            String value = safe(e.getValue());
            if (type.isBlank() || value.isBlank()) continue;

            ReviewTag tag = new ReviewTag(reviewId, type, value);
            saved.add(tagRepo.save(tag));
        }

        return saved;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
