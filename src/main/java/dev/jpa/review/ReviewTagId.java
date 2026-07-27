package dev.jpa.review;

import java.io.Serializable;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewTagId implements Serializable {
    private Long reviewId;
    private String tagType;
    private String tagValue;
}
