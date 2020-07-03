package org.matsim.contribs.analysis;

import lombok.Getter;
import lombok.ToString;
import org.matsim.api.core.v01.Coord;

@Getter
public class SetInformation {

    private final BoundingBox bbox = new BoundingBox();

    private double startTime = Double.POSITIVE_INFINITY;
    private double endTime = Double.NEGATIVE_INFINITY;

    void adjustStartAndEndTime(double time) {
        if (startTime > time) startTime = time;
        if (endTime < time) endTime = time;
    }

    @Getter
    @ToString
    static class BoundingBox {

        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;

        void adjust(Coord coord) {
            if (minX > coord.getX()) minX = coord.getX();
            if (minY > coord.getY()) minY = coord.getY();
            if (maxX < coord.getX()) maxX = coord.getX();
            if (maxY < coord.getY()) maxY = coord.getY();
        }
    }
}


