package org.matsim.contribs.analysis.parsing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geotools.data.FeatureWriter;
import org.geotools.filter.identity.FeatureIdImpl;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.analysis.store.LegSchema;
import org.matsim.contribs.analysis.store.LinkTripSchema;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.vehicles.Vehicle;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.*;

/**
 * This handler assumes all events are passed sequentially with increasing time
 */
@RequiredArgsConstructor
public class MovementHandler implements
        LinkEnterEventHandler, LinkLeaveEventHandler, TransitDriverStartsEventHandler,
        PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private final FeatureWriter<SimpleFeatureType, SimpleFeature> linkTripWriter;
    private final FeatureWriter<SimpleFeatureType, SimpleFeature> legWriter;
    private final Network network;

    // a person can only conduct one trip at a time
    private final Map<Id<Person>, Leg> startedLegs = new HashMap<>();
    // a vehicle can be only on one link at a given time
    private final Map<Id<Vehicle>, LinkEnterEvent> startedLinkTrips = new HashMap<>();
    // this has to be re-thought for multiple persons using a single vehicle like pt or drt
    private final Map<Id<Vehicle>, Id<Person>> personsInVehicle = new HashMap<>();

    // keep track of transit drivers and vehicles because they need to be treated differently
    private final Set<Id<Person>> transitDrivers = new HashSet<>();
    private final Set<Id<Vehicle>> transitVehicles = new HashSet<>();

    private static Date dateOf(double time) {
        var milliseconds = (long) (time * 1000);
        return new Date(milliseconds);
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent event) {

        transitDrivers.add(event.getDriverId());
        transitVehicles.add(event.getVehicleId());
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {

        // leave out pt for now
        if (transitVehicles.contains(event.getVehicleId())) return;

        startedLinkTrips.put(event.getVehicleId(), event);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {

        // leave out pt for now
        if (transitVehicles.contains(event.getVehicleId())) return;

        if (startedLinkTrips.containsKey(event.getVehicleId())) {
            var startedLinkTrip = startedLinkTrips.remove(event.getVehicleId());
            var personId = personsInVehicle.get(event.getVehicleId());
            var leg = startedLegs.get(personId);
            writeLinkTrip(startedLinkTrip, event.getTime(), personId, leg.getId(), leg.getMode());
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {

        // leave out pt for now
        if (transitVehicles.contains(event.getPersonId())) return;

        var leg = new Leg();
        leg.setStartTime(event.getTime());
        var coord = network.getLinks().get(event.getLinkId()).getToNode().getCoord(); // For now we assume agents start at the start of a link
        leg.setStartCoord(coord);
        leg.setStartLink(event.getLinkId());
        leg.setMode(event.getLegMode());
        leg.setPersonId(event.getPersonId());
        startedLegs.put(event.getPersonId(), leg);
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        // leave out pt for now
        if (transitVehicles.contains(event.getPersonId())) return;

        var leg = startedLegs.remove(event.getPersonId());
        leg.setEndLink(event.getLinkId());
        leg.setEndTime(event.getTime());
        var coord = network.getLinks().get(event.getLinkId()).getToNode().getCoord();
        leg.setEndCoord(coord);

        writeLeg(leg);
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {

        // leave out pt for now
        if (transitDrivers.contains(event.getPersonId())) return;

        var leg = startedLegs.get(event.getPersonId());

        if (transitVehicles.contains(event.getVehicleId())) {
            leg.setType(LegSchema.LegType.PT_PASSENGER);
        } else {
            leg.setType(LegSchema.LegType.NETWORK);
            personsInVehicle.put(event.getVehicleId(), event.getPersonId());
        } // possibly extend this with LegType.PT_VEHICLE

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {

        // leave out pt for now
        if (transitDrivers.contains(event.getPersonId())) return;

        personsInVehicle.remove(event.getVehicleId());

        // finish up last link trip assuming agent travelled to the end of the link - if one wants to do this correctly use relativePosition
        if (startedLinkTrips.containsKey(event.getVehicleId())) {
            var enterEvent = startedLinkTrips.remove(event.getVehicleId());
            var leg = startedLegs.get(event.getPersonId());
            writeLinkTrip(enterEvent, event.getTime(), event.getPersonId(), leg.getId(), leg.getMode());
        }

    }

    private void writeLinkTrip(LinkEnterEvent start, double endTime, Id<Person> personId, UUID legId, String mode) {

        var startCoord = network.getLinks().get(start.getLinkId()).getFromNode().getCoord();
        var endCoord = network.getLinks().get(start.getLinkId()).getToNode().getCoord();

        try {
            var toWrite = linkTripWriter.next();

            toWrite.setAttribute(LinkTripSchema.GEOMETRY, geometryFactory.createLineString(new Coordinate[]{
                    MGC.coord2Coordinate(startCoord), MGC.coord2Coordinate(endCoord)
            }));
            toWrite.setAttribute(LinkTripSchema.START_TIME, dateOf(start.getTime()));
            toWrite.setAttribute(LinkTripSchema.END_TIME, dateOf(endTime));
            toWrite.setAttribute(LinkTripSchema.PERSON_ID, personId);
            toWrite.setAttribute(LinkTripSchema.LEG_ID, legId);
            toWrite.setAttribute(LinkTripSchema.LINK_ID, start.getLinkId().toString());
            toWrite.setAttribute(LinkTripSchema.MODE, mode);

            linkTripWriter.write();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeLeg(Leg leg) {

        try {
            var toWrite = legWriter.next();
            ((FeatureIdImpl) toWrite.getIdentifier()).setID(leg.getId().toString());

            toWrite.setAttribute(LegSchema.GEOMETRY, geometryFactory.createLineString(new Coordinate[]{
                    MGC.coord2Coordinate(leg.getStartCoord()), MGC.coord2Coordinate(leg.getEndCoord())
            }));
            toWrite.setAttribute(LegSchema.START_LINK_ID, leg.getStartLink());
            toWrite.setAttribute(LegSchema.END_LINK_ID, leg.getEndLink());
            toWrite.setAttribute(LegSchema.START_TIME, dateOf(leg.getStartTime()));
            toWrite.setAttribute(LegSchema.END_TIME, dateOf(leg.getEndTime()));
            toWrite.setAttribute(LegSchema.TYPE, leg.getType().toString());
            toWrite.setAttribute(LegSchema.MODE, leg.getMode());
            toWrite.setAttribute(LegSchema.PERSON_ID, leg.getPersonId());

            legWriter.write();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    private static class Leg {

        private UUID id = UUID.randomUUID();
        private double startTime;
        private double endTime;
        private Coord startCoord;
        private Coord endCoord;
        private Id<Link> startLink;
        private Id<Link> endLink;
        private Id<Person> personId;
        private String mode;

        // make teleported the default. If a person enters vehicle event is encountered the type can be set differently
        private LegSchema.LegType type = LegSchema.LegType.TELEPORTED;
    }
}
