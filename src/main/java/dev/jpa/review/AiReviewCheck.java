package dev.jpa.review;

public class AiReviewCheck {
    public boolean allowed;
    public double toxicScore;     // 0~1
    public String flagReason;     // 운영/디버깅용
    public String sentiment;      // POS/NEU/NEG
}
