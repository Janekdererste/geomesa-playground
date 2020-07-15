package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LinkTrip {

    private final SimpleCoordinate from;
    private final SimpleCoordinate to;
    private final long startTime;
    private final long endTime;
}
