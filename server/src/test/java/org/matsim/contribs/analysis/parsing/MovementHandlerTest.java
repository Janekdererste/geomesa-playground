package org.matsim.contribs.analysis.parsing;

import lombok.extern.slf4j.Slf4j;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contribs.analysis.store.LinkTripSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.vehicles.Vehicle;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.Identifier;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class MovementHandlerTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void writeSingleLeg() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var store = new MatsimDataStore(storeRoot);
        var network = createSimpleNetwork();
        var pId = Id.createPersonId("1");
        var vId = Id.create("1", Vehicle.class);

        try (var legWriter = store.getLegWriter(); var linkTripWriter = store.getLinkTripWriter()) {

            var handler = new MovementHandler(linkTripWriter, legWriter, network);

            // access walk
            handler.handleEvent(new PersonDepartureEvent(1, pId, Id.createLinkId(1), TransportMode.walk));
            handler.handleEvent(new PersonArrivalEvent(14, pId, Id.createLinkId(1), TransportMode.walk));


        }
    }

    @Test
    public void writeAccessEgressTrip() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var eventsFile = testUtils.getInputDirectory() + "events.xml";
        var network = createSimpleNetwork();
        var store = new MatsimDataStore(storeRoot);

        try (var legWriter = store.getLegWriter(); var linkTripWriter = store.getLinkTripWriter()) {

            var handler = new MovementHandler(linkTripWriter, legWriter, network);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(handler);
            manager.initProcessing();
            new MatsimEventsReader(manager).readFile(eventsFile);
            manager.finishProcessing();
        }


        //TODO think about good assertions here. For now, I am happy if nothing crashes
        store.forEachLeg(Filter.INCLUDE, feature -> log.info(feature.toString()));

        Set<Identifier> legIds = new HashSet<>();

        store.forEachLinkTrip(Filter.INCLUDE, feature -> {
            log.info(feature.toString());
            var legId = (UUID) feature.getAttribute(LinkTripSchema.LEG_ID);
            legIds.add(new FeatureIdImpl(legId.toString()));
        });

        var ff = new FilterFactoryImpl();
        var idFilter = ff.id(legIds);

        store.forEachLeg(idFilter, feature -> log.info(feature.toString()));
    }

    private Network createSimpleNetwork() {

        var network = NetworkUtils.createNetwork();
        var node1 = network.getFactory().createNode(Id.createNodeId(1), new Coord(10, 10));
        var node2 = network.getFactory().createNode(Id.createNodeId(2), new Coord(20, 10));
        var node3 = network.getFactory().createNode(Id.createNodeId(3), new Coord(30, 10));
        var node4 = network.getFactory().createNode(Id.createNodeId(4), new Coord(40, 10));

        var link1 = network.getFactory().createLink(Id.createLinkId(1), node1, node2);
        var link2 = network.getFactory().createLink(Id.createLinkId(2), node2, node3);
        var link3 = network.getFactory().createLink(Id.createLinkId(3), node3, node4);


        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);
        network.addNode(node4);

        network.addLink(link1);
        network.addLink(link2);
        network.addLink(link3);

        return network;
    }

}