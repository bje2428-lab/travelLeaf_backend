package dev.jpa.bike_route;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "BIKE_ROUTE_PATH")
@Getter
public class BikeRoutePath {

    @Id
    @Column(name = "PATH_ID")
    private Long pathId;

    @Column(name = "ROUTE_ID")
    private Long routeId;   // FK지만 연관관계 ❌

    @Column(name = "SEQ")
    private Integer seq;

    @Column(name = "LAT")
    private Double lat;

    @Column(name = "LNG")
    private Double lng;
}