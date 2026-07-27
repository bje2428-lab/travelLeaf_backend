package dev.jpa.places.trend;

import jakarta.persistence.*;

@Entity
@Table(name = "REGION_TREND")
public class RegionTrend {
    @Id
    @Column(name = "trend_id")
    private Long trendId;

    protected RegionTrend() {}

    public Long getTrendId() { return trendId; }
    public void setTrendId(Long trendId) { this.trendId = trendId; }
}
