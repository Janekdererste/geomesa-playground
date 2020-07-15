package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SimpleLink {

    private final SimpleCoordinate from;
    private final SimpleCoordinate to;
    private final String id;
}
