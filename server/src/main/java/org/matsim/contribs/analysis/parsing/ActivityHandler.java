package org.matsim.contribs.analysis.parsing;

import lombok.RequiredArgsConstructor;
import org.geotools.data.FeatureWriter;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
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
        }
    }

    public void mergeWrapAroundActivities() {

        for (Map.Entry<String, ActivityEndEvent> entry : wrapAroundActivities.entrySet()) {

            if (startedActivities.containsKey(entry.getKey())) {
                var start = startedActivities.get(entry.getKey());
                var end = entry.getValue();
                writeActivity(start, end.getTime());
            }
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
     * This assumes that a person will only ever do one activity at the same time
     */
    private String createKey(Id<Person> personId, String actType) {
        return personId.toString() + "_" + actType;
    }

    /**
     * Takes coord from event if possible, then from facility if possible, then from link as fallback
     */
    private Coord extractLocation(ActivityStartEvent event) {

        if (event.getCoord() != null) return event.getCoord();

        if (event.getFacilityId() != null && facilities != null && facilities.getFacilities().containsKey(event.getFacilityId()))
            return facilities.getFacilities().get(event.getFacilityId()).getCoord();

        if (event.getLinkId() != null && network.getLinks().containsKey(event.getLinkId()))
            return network.getLinks().get(event.getLinkId()).getCoord();

        throw new RuntimeException("Could not retreive coordinate for event: " + event.toString());
    }


}
