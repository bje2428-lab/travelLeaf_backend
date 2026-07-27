package dev.jpa.bike;

import java.util.List;

import lombok.Getter;

@Getter
public class PathSearchRequest {

    private double radius;   // km
    private List<PathPoint> path;

    @Getter
    public static class PathPoint {
        private double lat;
        private double lng;
    }
}
