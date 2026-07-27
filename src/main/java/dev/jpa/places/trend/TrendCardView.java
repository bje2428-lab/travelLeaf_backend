package dev.jpa.places.trend;

public interface TrendCardView {
    Long getTrendId();
    Long getRegionId();
    String getRegionName();
    Long getCityId();
    String getCityName();
    Double getScore();
    Integer getRankNo();

    String getSubtitle();
    String getKeywords();
    String getHashtags();
    String getOneLineReason();
}
