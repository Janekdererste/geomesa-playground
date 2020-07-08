package org.matsim.contribs.analysis.parsing;

import lombok.RequiredArgsConstructor;
import org.matsim.contribs.analysis.store.MatsimRunStore;
import org.xml.sax.Attributes;

@RequiredArgsConstructor
public class MovementHandler {

    private final MatsimRunStore store;

    public void handleDeparture(double time, Attributes atts) {
    }

    public void handleArrival(double time, Attributes atts) {
    }

    public void handleEntersVehicle(double time, Attributes atts) {
    }

    public void handleLinkEnter(double time, Attributes atts) {
    }

    public void handleLinkLeave(double time, Attributes atts) {
    }

    public void handleTransitDriverStarts(double time, Attributes atts) {
    }
}
