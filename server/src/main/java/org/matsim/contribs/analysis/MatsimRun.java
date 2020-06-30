package org.matsim.contribs.analysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MatsimRun {

    private final String networkFile;
    private final String eventsFile;
}
