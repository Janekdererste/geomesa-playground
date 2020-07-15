package org.matsim.contribs.analysis.api;

import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;

@Getter
public class SimpleCoordinate {

    private final double x;
    private final double y;

    public SimpleCoordinate(Coordinate coordinate) {
        this.x = coordinate.getX();
        this.y = coordinate.getY();
    }
}
