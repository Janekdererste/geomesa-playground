package org.matsim.contribs.analysis.parsing;

import lombok.RequiredArgsConstructor;
import org.geotools.data.FeatureWriter;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.analysis.store.ActivitySchema;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.facilities.ActivityFacilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ActivityHandler implements ActivityStartEventHandler, ActivityEndEventHandler {

    private final FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
    private final Map<String, ActivityStartEvent> startedActivities = new HashMap<>();
    private final Map<String, ActivityEndEvent> wrapAroundActivities = new HashMap<>();

    // for now, we'll put in null for facilities. I guess this could be solved more elegantly
    private final Network network;
    private final ActivityFacilities facilities;

    @Override
    public void handleEvent(ActivityStartEvent event) {

        var key = createKey(event.getPersonId(), event.getActType());
        startedActivities.put(key, event);
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {

        var key = createKey(event.getPersonId(), event.getActType());
        if (startedActivities.containsKey(key)) {
            var startEvent = startedActivities.remove(key);
            writeActivity(startEvent, event.getTime());
        } else {
            writeActivity(event, event.getTime());
        }
    }

    /**
     * write all the left over activities such as the last home activity of the day with a duration nof 0
     */
    public void writeUnfinishedActivities() {
        for (var activity : startedActivities.values()) {
            writeActivity(activity, activity.getTime());
        }
    }

    private void writeActivity(ActivityStartEvent start, double endTime) {

        try {
            var toWrite = writer.next();

            var location = extractLocation(start);
            toWrite.setAttribute(ActivitySchema.GEOMETRY, MGC.coord2Point(location));
            toWrite.setAttribute(ActivitySchema.START_TIME, Date.from(Instant.ofEpochSecond((long) start.getTime())));
            toWrite.setAttribute(ActivitySchema.END_TIME, Date.from(Instant.ofEpochSecond((long) endTime)));
            toWrite.setAttribute(ActivitySchema.TYPE, start.getActType());
            toWrite.setAttribute(ActivitySchema.PERSON_ID, start.getPersonId());
            toWrite.setAttribute(ActivitySchema.FACILITY_ID, start.getFacilityId());
            toWrite.setAttribute(ActivitySchema.LINK_ID, start.getLinkId());

            writer.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Have this more or less duplicate write method here to write un-started activities such as the first home activity
     * Because act-start and act-end classes share no meaningfull interface I don't know how to do this in a better way
     */
    private void writeActivity(ActivityEndEvent end, double startTime) {

        try {
            var toWrite = writer.next();

            var location = extractLocation(end);
            toWrite.setAttribute(ActivitySchema.GEOMETRY, MGC.coord2Point(location));
            toWrite.setAttribute(ActivitySchema.START_TIME, Date.from(Instant.ofEpochSecond((long) end.getTime())));
            toWrite.setAttribute(ActivitySchema.END_TIME, Date.from(Instant.ofEpochSecond((long) startTime)));
            toWrite.setAttribute(ActivitySchema.TYPE, end.getActType());
            toWrite.setAttribute(ActivitySchema.PERSON_ID, end.getPersonId());
            toWrite.setAttribute(ActivitySchema.FACILITY_ID, end.getFacilityId());
            toWrite.setAttribute(ActivitySchema.LINK_ID, end.getLinkId());

            writer.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This assumes that a person will only ever do one activity at the same time
     */
    private String createKey(Id<Person> personId, String actType) {
        return personId.toString() + "_" + actType;
    }

    /**
     * Takes coord from event if possible, then from facility if possible, then from link as fallback
     * This seems to be very messy. Is there such a thing as a union type in Java?
     */
    private Coord extractLocation(Event event) {

        if (event instanceof ActivityStartEvent) {
            var start = (ActivityStartEvent) event;
            if (start.getCoord() != null) return start.getCoord();
        }

        if (event instanceof HasFacilityId) {
            var hasFacility = (HasFacilityId) event;
            if (hasFacility.getFacilityId() != null && facilities != null && facilities.getFacilities().containsKey(hasFacility.getFacilityId()))
                return facilities.getFacilities().get(hasFacility.getFacilityId()).getCoord();
        }

        if (event instanceof HasLinkId) {
            var hasLink = (HasLinkId) event;
            if (hasLink.getLinkId() != null && network.getLinks().containsKey(hasLink.getLinkId()))
                return network.getLinks().get(hasLink.getLinkId()).getCoord();
        }

        throw new RuntimeException("Could not retreive coordinate for event: " + event.toString());
    }


}
