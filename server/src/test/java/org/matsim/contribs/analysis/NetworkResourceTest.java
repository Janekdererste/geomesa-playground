package org.matsim.contribs.analysis;

import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.network.NetworkUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetworkResourceTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void testEquilNetwork() throws IOException {

        var networkPath = ExamplesUtils.getTestScenarioURL("equil").toString() + "network.xml";

        // read data into store, using matsim equil-network. Using pseudo mercartor projection which is close to Atlantis crs used by equil network
        var store = new GeomesaFileSystemStore(testUtils.getOutputDirectory() + "store");
        IngestDataFromLocalDisk.ingestNetwork(store, networkPath, "EPSG:3857");

        var resource = new NetworkResource(store);
        var result = resource.getNetworkAsJson();
        var expectedNetwork = NetworkUtils.readNetwork(networkPath);

        for (NetworkResource.SimpleLink simpleLink : result) {

            assertTrue(expectedNetwork.getLinks().containsKey(Id.createLinkId(simpleLink.getLinkId())));
            var link = expectedNetwork.getLinks().get(Id.createLinkId(simpleLink.getLinkId()));

            compareWithRounding(link.getFromNode().getCoord(), simpleLink.getFrom());
            compareWithRounding(link.getToNode().getCoord(), simpleLink.getTo());
        }
    }

    private void compareWithRounding(Coord expected, Coordinate actual) {

        // pick a large delta to buffer rounding errors from coordinate transformation
        assertEquals(expected.getX(), actual.getX(), 0.1);
        assertEquals(expected.getY(), actual.getY(), 0.1);
    }
}