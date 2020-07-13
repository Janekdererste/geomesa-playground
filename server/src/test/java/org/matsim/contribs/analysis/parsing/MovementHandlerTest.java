package org.matsim.contribs.analysis.parsing;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.core.network.NetworkUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.vehicles.Vehicle;

import java.io.IOException;

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

        try (var legWriter = store.getLegWriter(); var linkTripWriter = store.getLinkMovementWriter()) {

            var handler = new MovementHandler(linkTripWriter, legWriter, network);

            handler.handleEvent(new PersonDepartureEvent(1, pId, Id.createLinkId(1), TransportMode.car));

        }
    }

    private Network createSimpleNetwork() {

        var network = NetworkUtils.createNetwork();
        var node1 = network.getFactory().createNode(Id.createNodeId("1"), new Coord(10, 10));
        var node2 = network.getFactory().createNode(Id.createNodeId("2"), new Coord(110, 10));
        var node3 = network.getFactory().createNode(Id.createNodeId("3"), new Coord(220, 10));

        var link1 = network.getFactory().createLink(Id.createLinkId(1), node1, node2);
        var link2 = network.getFactory().createLink(Id.createLinkId(2), node2, node3);

        network.addNode(node1);
        network.addNode(node2);
        network.addNode(node3);

        network.addLink(link1);
        network.addLink(link2);

        return network;
    }

}