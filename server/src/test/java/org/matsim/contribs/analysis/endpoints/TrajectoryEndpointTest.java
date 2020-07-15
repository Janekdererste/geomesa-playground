package org.matsim.contribs.analysis.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.contribs.analysis.store.LinkTripSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.testcases.MatsimTestUtils;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Slf4j
public class TrajectoryEndpointTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void simpleTest() throws IOException {

        var store = new MatsimDataStore(testUtils.getOutputDirectory() + "store");

        try (var writer = store.getLinkTripWriter()) {

            var toWrite = writer.next();

            toWrite.setAttribute(LinkTripSchema.GEOMETRY, "LINESTRING (0 0, 10 10)");
            toWrite.setAttribute(LinkTripSchema.START_TIME, new Date(0));
            toWrite.setAttribute(LinkTripSchema.END_TIME, new Date(600 * 1000)); // a 10 minutes link trip
            toWrite.setAttribute(LinkTripSchema.PERSON_ID, "1");
            toWrite.setAttribute(LinkTripSchema.LEG_ID, UUID.randomUUID());

            log.info(toWrite.toString());
            writer.write();

            var toWrite2 = writer.next();

            toWrite2.setAttribute(LinkTripSchema.GEOMETRY, "LINESTRING (0 0, 10 10)");
            toWrite2.setAttribute(LinkTripSchema.START_TIME, new Date(601 * 1000));
            toWrite2.setAttribute(LinkTripSchema.END_TIME, new Date(1200 * 1000)); // a 10 minutes link trip
            toWrite2.setAttribute(LinkTripSchema.PERSON_ID, "1");
            toWrite2.setAttribute(LinkTripSchema.LEG_ID, UUID.randomUUID());

            log.info(toWrite2.toString());
            writer.write();
        }

        var endpoint = new TrajectoryEndpoint(store);

        // this should get the second feature since it crosses this time period
        var result = endpoint.getTrajectories(0, 3600);

        assertEquals(1, result.size());
        var linkTrip = result.iterator().next();
        assertEquals(601, linkTrip.getStartTime());
    }

}