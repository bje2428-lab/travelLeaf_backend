package dev.jpa.bike_route;

public interface BikeRouteProjection {

    Long getRouteId();
    String getRouteName();
    String getRegion();
    String getCity();

    Double getTotalDistanceKm();
    Integer getEstimatedTimeMin();

    String getDescription();
    String getHighlights();
    String getFoodInfo();
    String getTips();
}
