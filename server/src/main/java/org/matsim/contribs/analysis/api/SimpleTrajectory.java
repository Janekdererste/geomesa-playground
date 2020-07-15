package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class SimpleTrajectory {

    // exchange for another representation
    private final List<SimpleCoordinate> coords;
    private final List<Double> times;
}
