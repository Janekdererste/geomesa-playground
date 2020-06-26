package org.matism.contribs.analysis;

import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.matsim.api.core.v01.network.Network;

@RequiredArgsConstructor
@Getter
public class MatsimRun {

    private final String networkFile;
    private final String eventsFile;
}
