package dev.jpa.location;

import jakarta.persistence.*;

@Entity
@Table(name = "CITY")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_seq")
    @SequenceGenerator(
            name = "city_seq",
            sequenceName = "SEQ_CITY",
            allocationSize = 1
    )
    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;
    
    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    // CITY(N) : REGION(1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    protected City() {
    }

    public City(String cityName, Region region) {
        this.cityName = cityName;
        this.region = region;
    }

    public Long getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public Region getRegion() {
        return region;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
