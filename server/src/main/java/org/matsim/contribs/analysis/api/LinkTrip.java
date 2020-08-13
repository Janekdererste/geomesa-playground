package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LinkTrip {

    private final SimpleCoordinate fromCoordinate;
    private final SimpleCoordinate toCoordinate;
    private final long startTime;
    private final long endTime;
    private final String mode;
    private final String personId;
}
