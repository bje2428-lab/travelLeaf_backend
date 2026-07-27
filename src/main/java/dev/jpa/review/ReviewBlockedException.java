package dev.jpa.review;

public class ReviewBlockedException extends RuntimeException {
    private final double toxicScore;
    private final String sentiment;
    private final String flagReason;

    public ReviewBlockedException(String message, double toxicScore, String sentiment, String flagReason) {
        super(message);
        this.toxicScore = toxicScore;
        this.sentiment = sentiment;
        this.flagReason = flagReason;
    }

    public double getToxicScore() { return toxicScore; }
    public String getSentiment() { return sentiment; }
    public String getFlagReason() { return flagReason; }
}
