package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LinkTrip {

    private final SimpleCoordinate from;
    private final SimpleCoordinate to;
    private final long fromTime;
    private final long toTime;
    private final String mode;
}
