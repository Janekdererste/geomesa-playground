package org.matsim.contribs.analysis.parsing;

import lombok.extern.slf4j.Slf4j;
import org.geotools.filter.FilterFactoryImpl;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.contribs.analysis.store.ActivitySchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.facilities.ActivityFacility;
import org.matsim.testcases.MatsimTestUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@Slf4j
public class ActivityHandlerTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void testIndexInterval() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var store = new MatsimDataStore(storeRoot);
        var location = new Coord(1, 1);

        try (var writer = store.getActivityWriter()) {

            var handler = new ActivityHandler(writer, null);
            var startEvent = new ActivityStartEvent(
                    1,
                    Id.createPersonId("1"),
                    Id.createLinkId("1"),
                    Id.create("1", ActivityFacility.class),
                    "some type",
                    location
            );
            var endEvent = new ActivityEndEvent(
                    2,
                    Id.createPersonId("1"),
                    Id.createLinkId("1"),
                    Id.create("1", ActivityFacility.class),
                    "some type"
            );

            handler.handleEvent(startEvent);
            handler.handleEvent(endEvent); // this should trigger a write

            // have another event pair with a nother time period
            var otherStartEvent = new ActivityStartEvent(
                    3600,
                    Id.createPersonId("10"),
                    Id.createLinkId("1"),
                    Id.create("1", ActivityFacility.class),
                    "some type",
                    location
            );

            var otherEndEvent = new ActivityEndEvent(
                    3600 + 300,
                    Id.createPersonId("10"),
                    Id.createLinkId("1"),
                    Id.create("1", ActivityFacility.class),
                    "some type"
            );

            handler.handleEvent(otherStartEvent);
            handler.handleEvent(otherEndEvent);
        }

        var ff = new FilterFactoryImpl();

        // test for temporal window this should only fetch activity of person with
        var time = Date.from(Instant.ofEpochSecond(10L));
        var temporalFilter = ff.after(ff.property(ActivitySchema.START_TIME), ff.literal(time));

        store.forEachActivity(temporalFilter, feature -> {
            assertEquals("10", feature.getAttribute(ActivitySchema.PERSON_ID));
            assertEquals("1", feature.getAttribute(ActivitySchema.LINK_ID));
            assertEquals("1", feature.getAttribute(ActivitySchema.FACILITY_ID));
            assertEquals(new Date(3600000L), feature.getAttribute(ActivitySchema.START_TIME));
            assertEquals(new Date(3900000L), feature.getAttribute(ActivitySchema.END_TIME));
            assertEquals(1, ((Point) feature.getAttribute(ActivitySchema.GEOMETRY)).getX(), 0.0001);
            assertEquals(1, ((Point) feature.getAttribute(ActivitySchema.GEOMETRY)).getY(), 0.0001);
        });
    }
}