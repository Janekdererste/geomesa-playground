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

@Log4j2
public class TrajectoryToGeomesaHandler implements LinkEnterEventHandler, LinkLeaveEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {

    private final Map<Id<Person>, Leg> departed = new HashMap<>();
    private final Map<Id<Vehicle>, Id<Person>> personInVehicle = new HashMap<>();
    private final FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
    private final Network network;
    private int counter = 0;

    TrajectoryToGeomesaHandler(FeatureWriter<SimpleFeatureType, SimpleFeature> writer, Network network) {
        this.writer = writer;
        this.network = network;
    }

    @Override
    public void handleEvent(PersonDepartureEvent personDepartureEvent) {

        // this could probably get a cache
        var trip = new Leg();
        trip.times.add(personDepartureEvent.getTime());
        trip.mode = personDepartureEvent.getLegMode();
        var fromCoord = network.getLinks().get(personDepartureEvent.getLinkId()).getToNode().getCoord(); // I think we assume agents start at the end of a link
        trip.coords.add(fromCoord);
        trip.linkIds.add(personDepartureEvent.getLinkId().toString());

        departed.put(personDepartureEvent.getPersonId(), trip);
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent personEntersVehicleEvent) {

        // track who is in which vehicle -- this is not fit for scenarios with pt yet
        personInVehicle.put(personEntersVehicleEvent.getVehicleId(), personEntersVehicleEvent.getPersonId());

        // if a person enters a vehicle the leg is not teleported
        var leg = departed.get(personEntersVehicleEvent.getPersonId());
        leg.isTeleported = false;
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {

        personInVehicle.remove(personLeavesVehicleEvent.getVehicleId());
    }

    @Override
    public void handleEvent(PersonArrivalEvent personArrivalEvent) {



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
    public void handleEvent(LinkEnterEvent linkEnterEvent) {

       /* var link = network.getLinks().get(linkEnterEvent.getLinkId());
        var person = personInVehicle.get(linkEnterEvent.getVehicleId());
        var leg = departed.get(person);
        leg.coords.add(link.getFromNode().getCoord());
        leg.times.add(linkEnterEvent.getTime());

        */
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {

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
            toWrite.setAttribute(TrajectoryFeatureType.agentId, personId.toString());

            // assume we have at least two values for time
            toWrite.setAttribute(TrajectoryFeatureType.enterTime, new Date(leg.times.get(0).longValue()));
            toWrite.setAttribute(TrajectoryFeatureType.exitTime, new Date(leg.times.get(leg.times.size() - 1).longValue()));

            toWrite.setAttribute(TrajectoryFeatureType.isTeleported, leg.isTeleported);
            toWrite.setAttribute(TrajectoryFeatureType.mode, leg.mode);
            toWrite.setAttribute(TrajectoryFeatureType.geometry, createLineStringString(leg));

            writer.write();
            counter++;

            if (counter % 10 == 0) {
                log.info("wrote: " + counter + " events");
                log.info("current feature: " + toWrite.toString());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createLineStringString(Leg leg) {

        var builder = new StringBuilder("LINESTRING (");
        for (Coord coord : leg.coords) {
            builder.append(coord.getX());
            builder.append(" ");
            builder.append(coord.getY());
            builder.append(",");
        }
        builder.append(")");
        return builder.toString();
    }

    private static class Leg {

        private final List<Coord> coords = new ArrayList<>();
        private final List<Double> times = new ArrayList<>(); // replace this with an unboxed implementation
        private final List<String> linkIds = new ArrayList<>();
        private String mode;
        private boolean isTeleported = true;
    }



}
