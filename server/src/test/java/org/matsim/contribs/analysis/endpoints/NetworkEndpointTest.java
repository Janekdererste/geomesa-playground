package org.matsim.contribs.analysis.endpoints;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.contribs.analysis.store.LinkSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.testcases.MatsimTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NetworkEndpointTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void simpleTest() throws IOException {

        var store = new MatsimDataStore(testUtils.getOutputDirectory() + "store");

        // we need some abstraction to translate links into features
        try (var writer = store.getLinkWriter()) {

            var toWrite = writer.next();
            toWrite.setAttribute(LinkSchema.GEOMETRY, "LINESTRING(0 0, 10 10)");
            toWrite.setAttribute(LinkSchema.LINK_ID, "1");
            toWrite.setAttribute(LinkSchema.FROM_NODE_ID, "1");
            toWrite.setAttribute(LinkSchema.TO_NODE_ID, "2");
            toWrite.setAttribute(LinkSchema.ALLOWED_MODES, "car");
            toWrite.setAttribute(LinkSchema.CAPACITY, 1);
            toWrite.setAttribute(LinkSchema.FREESPEED, 1);
            toWrite.setAttribute(LinkSchema.LENGTH, 1);
            writer.write();

            var other = writer.next();
            other.setAttribute(LinkSchema.GEOMETRY, "LINESTRING(0 0, 10 10)");
            other.setAttribute(LinkSchema.LINK_ID, "2");
            other.setAttribute(LinkSchema.FROM_NODE_ID, "1");
            other.setAttribute(LinkSchema.TO_NODE_ID, "2");
            other.setAttribute(LinkSchema.ALLOWED_MODES, "other-mode");
            other.setAttribute(LinkSchema.CAPACITY, 1);
            other.setAttribute(LinkSchema.FREESPEED, 1);
            other.setAttribute(LinkSchema.LENGTH, 1);
            writer.write();
        }

        var endpoint = new NetworkEndpoint(store);

        var result = endpoint.getTrajectoryAsJson(List.of("car"));

        assertEquals(1, result.size());
        var link = result.iterator().next();
        assertEquals("1", link.getId());
    }
}