package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SimpleLink {

    private final SimpleCoordinate fromCoordinate;
    private final SimpleCoordinate toCoordinate;
    private final String id;
}
