package dev.jpa.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class ReviewDetailResponseDTO {

    // ===== Review fields =====
    private Long reviewId;
    private String city;
    private String district;
    private String placeName;
    private String userId;
    private Integer rating;
    private String content;

    private Date createdAt;
    private Date updatedAt;

    private Double toxicScore;
    private String flagReason;
    private String sentiment;

    private LocalDateTime moderatedAt;

    private String aiSummary;
    private String aiKeywords;
    private LocalDateTime aiSummaryAt;

    // ===== Tags (optional) =====
    private List<ReviewTag> tags;

    // ===== Comments =====
    private List<ReviewCommentResponseDTO> comments;

    // 댓글 페이지 정보(선택)
    private int commentPage;
    private int commentSize;
    private long commentTotalElements;
    private int commentTotalPages;
}
