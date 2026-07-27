package dev.jpa.notice;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "NOTICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq")
    @SequenceGenerator(name = "notice_seq", sequenceName = "notice_seq", allocationSize = 1)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;  // USER_TB.user_id

    @Column(name = "is_fixed", length = 1)
    private String isFixed; // Y / N

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "category", length = 100)
    private String category;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
