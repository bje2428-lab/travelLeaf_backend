package dev.jpa.review;

import lombok.Data;

@Data
public class ReviewDTO {

    private String city;
    private String district;
    private String placeName;

    private String userId;
    private Integer rating;
    private String content;
}
