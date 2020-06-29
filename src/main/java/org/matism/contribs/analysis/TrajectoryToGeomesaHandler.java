package org.matism.contribs.analysis;

import lombok.extern.log4j.Log4j2;
import org.geotools.data.FeatureWriter;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.AgentWaitingForPtEvent;
import org.matsim.core.api.experimental.events.handler.AgentWaitingForPtEventHandler;
import org.matsim.vehicles.Vehicle;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

import static org.matism.contribs.analysis.GeomesaFileSystemStore.TrajectorySchema.*;

@Log4j2
public class TrajectoryToGeomesaHandler implements TransitDriverStartsEventHandler, LinkLeaveEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {

    private final Map<Id<Person>, Leg> departed = new HashMap<>();
    private final Map<Id<Vehicle>, Id<Person>> personInVehicle = new HashMap<>();
    private final FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
    private final Set<Id<Person>> transitDrivers = new HashSet<>();
    private final Network network;
    private int counter = 0;

    public TrajectoryToGeomesaHandler(FeatureWriter<SimpleFeatureType, SimpleFeature> writer, Network network) {
        this.writer = writer;
        this.network = network;
    }

    @Override
    public void handleEvent(PersonDepartureEvent personDepartureEvent) {

        if (transitDrivers.contains(personDepartureEvent.getPersonId())) return;
        // this could probably get a cache
        var leg = new Leg();
        leg.times.add(personDepartureEvent.getTime());
        leg.mode = personDepartureEvent.getLegMode();
        var coord = network.getLinks().get(personDepartureEvent.getLinkId()).getToNode().getCoord(); // For now we assume agents start at the start of a link
        leg.coords.add(coord);
        leg.linkIds.add(personDepartureEvent.getLinkId().toString());

        departed.put(personDepartureEvent.getPersonId(), leg);
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent personEntersVehicleEvent) {

        if (transitDrivers.contains(personEntersVehicleEvent.getPersonId())) return;

        // if a person enters a vehicle the leg is not teleported
        var leg = departed.get(personEntersVehicleEvent.getPersonId());
        leg.isTeleported = false;
        leg.vehicleId = personEntersVehicleEvent.getVehicleId().toString();

        if (!leg.mode.equals("pt"))
            // track who is in which vehicle -- this is not fit for scenarios with pt yet
            personInVehicle.put(personEntersVehicleEvent.getVehicleId(), personEntersVehicleEvent.getPersonId());

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {

        if (transitDrivers.contains(personLeavesVehicleEvent.getPersonId())) return;

        personInVehicle.remove(personLeavesVehicleEvent.getVehicleId());
    }

    @Override
    public void handleEvent(PersonArrivalEvent personArrivalEvent) {

        if (transitDrivers.contains(personArrivalEvent.getPersonId())) return;

        // a leg always ends here
        var link = network.getLinks().get(personArrivalEvent.getLinkId());
        var leg = departed.get(personArrivalEvent.getPersonId());
        leg.coords.add(link.getToNode().getCoord()); // assume people travel until end of a link
        leg.times.add(personArrivalEvent.getTime());
        leg.linkIds.add(personArrivalEvent.getLinkId().toString()); // add last link

        departed.remove(personArrivalEvent.getPersonId());

        writeLeg(personArrivalEvent.getPersonId(), leg);
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {

        if (!personInVehicle.containsKey(linkLeaveEvent.getVehicleId())) return;

        var person = personInVehicle.get(linkLeaveEvent.getVehicleId());
        var leg = departed.get(person);

        // handle if departure event has already put in the first coordinate
        if (leg.linkIds.size() == 1 && leg.linkIds.get(0).equals(linkLeaveEvent.getLinkId().toString())) {
            return;
        }

        var link = network.getLinks().get(linkLeaveEvent.getLinkId());
        leg.coords.add(link.getToNode().getCoord());
        leg.times.add(linkLeaveEvent.getTime());
        leg.linkIds.add(linkLeaveEvent.getLinkId().toString());
    }

    private void writeLeg(Id<Person> personId, Leg leg) {

        try {
            var toWrite = writer.next();
            toWrite.setAttribute(AGENT_ID, personId.toString());

            // assume we have at least two values for time
            toWrite.setAttribute(ENTER_TIME, Date.from(Instant.ofEpochSecond(leg.times.get(0).longValue())));
            toWrite.setAttribute(EXIT_TIME, Date.from(Instant.ofEpochSecond(leg.times.get(leg.times.size() - 1).longValue())));

            toWrite.setAttribute(IS_TELEPORTED, leg.isTeleported);
            toWrite.setAttribute(MODE, leg.mode);
            toWrite.setAttribute(VEHICLE_ID, leg.vehicleId);

            toWrite.setAttribute(LINK_IDS, leg.linkIds);
            toWrite.setAttribute(TIMES, leg.times);
            toWrite.setAttribute(GEOMETRY, createLineStringString(leg));

            writer.write();
            counter++;

            if (counter % 1000 == 0) {
                log.info("wrote: " + counter + " events, timestep is: " + leg.times.get(leg.times.size() - 1));
                log.info("current feature: " + toWrite.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createLineStringString(Leg leg) {

        var builder = new StringBuilder("LINESTRING (");

        for (int i = 0; i < leg.coords.size(); i++) {
            var coord = leg.coords.get(i);
            builder.append(coord.getX());
            builder.append(" ");
            builder.append(coord.getY());
            if (i != leg.coords.size() - 1) builder.append(","); // no comma for last value
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public void handleEvent(TransitDriverStartsEvent transitDriverStartsEvent) {
        transitDrivers.add(transitDriverStartsEvent.getDriverId());
    }

    private static class Leg {

        private final List<Coord> coords = new ArrayList<>();
        private final List<Double> times = new ArrayList<>(); // replace this with an unboxed implementation
        private final List<String> linkIds = new ArrayList<>();
        private String mode;
        private String vehicleId;
        private boolean isTeleported = true;
    }



}
