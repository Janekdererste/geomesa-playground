package org.matsim.contribs.analysis.parsing;

import lombok.RequiredArgsConstructor;
import org.geotools.data.FeatureWriter;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.analysis.store.ActivitySchema;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.facilities.ActivityFacility;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ActivityHandler {

    private final FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
    private final Map<String, ActivityStartEvent> startedActivities = new HashMap<>();
    private final Map<String, ActivityEndEvent> wrapAroundActivities = new HashMap<>();
    private final Scenario scenario;

    public void handleStart(double time, Attributes atts) {

        var facilityId = atts.getValue(HasFacilityId.ATTRIBUTE_FACILITY) == null ? null : Id.create(atts.getValue(HasFacilityId.ATTRIBUTE_FACILITY), ActivityFacility.class);
        var linkId = Id.create(atts.getValue(HasLinkId.ATTRIBUTE_LINK), Link.class);
        var event = new ActivityStartEvent(
                time,
                Id.create(atts.getValue(HasPersonId.ATTRIBUTE_PERSON), Person.class),
                linkId,
                facilityId,
                atts.getValue(ActivityStartEvent.ATTRIBUTE_ACTTYPE),
                extractLocation(atts, facilityId, linkId));
        var key = createKey(event.getPersonId(), event.getActType());
        startedActivities.put(key, event);
    }

    public void handleEnd(double time, Attributes atts) {

        var personId = Id.create(atts.getValue(HasPersonId.ATTRIBUTE_PERSON), Person.class);
        var actType = atts.getValue(ActivityEndEvent.ATTRIBUTE_ACTTYPE);

        var key = createKey(personId, actType);
        if (startedActivities.containsKey(key)) {
            var activity = startedActivities.remove(key);
            writeActivity(activity, time);
        } else {

            var facilityId = atts.getValue(HasFacilityId.ATTRIBUTE_FACILITY) == null ? null : Id.create(atts.getValue(HasFacilityId.ATTRIBUTE_FACILITY), ActivityFacility.class);
            var linkId = Id.create(atts.getValue(HasLinkId.ATTRIBUTE_LINK), Link.class);
            var event = new ActivityEndEvent(
                    time,
                    personId,
                    linkId,
                    facilityId,
                    actType
            );

            wrapAroundActivities.put(key, event);
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

            toWrite.setAttribute(ActivitySchema.START_TIME, Date.from(Instant.ofEpochSecond((long) start.getTime())));
            toWrite.setAttribute(ActivitySchema.END_TIME, Date.from(Instant.ofEpochSecond((long) endTime)));
            toWrite.setAttribute(ActivitySchema.GEOMETRY, "POINT(" + start.getCoord().getX() + " " + start.getCoord().getY() + ")");
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
    private Coord extractLocation(Attributes atts, Id<ActivityFacility> facilityId, Id<Link> linkId) {

        if (atts.getValue(Event.ATTRIBUTE_X) != null && atts.getValue(Event.ATTRIBUTE_Y) != null) {
            return new Coord(Double.parseDouble(atts.getValue(Event.ATTRIBUTE_X)), Double.parseDouble(Event.ATTRIBUTE_Y));
        }
        if (facilityId != null && scenario.getActivityFacilities().getFacilities().containsKey(facilityId)) {
            return scenario.getActivityFacilities().getFacilities().get(facilityId).getCoord();
        }
        if (linkId != null && scenario.getNetwork().getLinks().containsKey(linkId)) {
            return scenario.getNetwork().getLinks().get(linkId).getCoord();
        }
        throw new RuntimeException("Could not retreive coordinate for event: " + atts.toString());
    }
}
