package dev.jpa.routing.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public class BikeDistanceReq {

    public enum Style { REGULAR, ROAD, MOUNTAIN }

    public static class Point {
        @JsonAlias({"lat", "y"})
        public Double lat;

        @JsonAlias({"lng", "x"})
        public Double lng;
    }

    // start + waypoints + end
    public List<Point> points;

    // optional
    public Style style = Style.REGULAR;
}
