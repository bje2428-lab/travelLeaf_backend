package dev.jpa.review;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "REVIEW_TAG")
@IdClass(ReviewTagId.class)
@Getter @Setter
@NoArgsConstructor
public class ReviewTag {

    @Id
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @Id
    @Column(name = "TAG_TYPE")
    private String tagType;

    @Id
    @Column(name = "TAG_VALUE")
    private String tagValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt = new Date();

    public ReviewTag(Long reviewId, String tagType, String tagValue) {
        this.reviewId = reviewId;
        this.tagType = tagType;
        this.tagValue = tagValue;
        this.createdAt = new Date();
    }
}
