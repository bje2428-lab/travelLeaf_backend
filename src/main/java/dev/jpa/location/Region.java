package dev.jpa.location;

import jakarta.persistence.*;

@Entity
@Table(name = "REGION")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq")
    @SequenceGenerator(
            name = "region_seq",
            sequenceName = "SEQ_REGION",
            allocationSize = 1
    )
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_name", length = 100, nullable = false)
    private String regionName;

    // 🔥 추가
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    protected Region() {
    }

    public Region(String regionName) {
        this.regionName = regionName;
    }

    public Long getRegionId() {
        return regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
