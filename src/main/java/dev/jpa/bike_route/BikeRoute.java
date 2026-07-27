package dev.jpa.bike_route;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "BIKE_ROUTE")
@Getter
public class BikeRoute {

    @Id
    @Column(name = "ROUTE_ID")
    private Long routeId;

    @Column(name = "ROUTE_NAME")
    private String routeName;

    @Column(name = "REGION")
    private String region;

    @Column(name = "CITY")
    private String city;

    @Column(name = "TOTAL_DISTANCE_KM")
    private Double totalDistanceKm;

    @Column(name = "ESTIMATED_TIME_MIN")
    private Integer estimatedTimeMin;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @Column(name = "HIGHLIGHTS")
    @Lob
    private String highlights;   // JSON 문자열

    @Column(name = "FOOD_INFO")
    @Lob
    private String foodInfo;     // JSON 문자열

    @Column(name = "TIPS")
    @Lob
    private String tips;         // JSON 문자열
}
