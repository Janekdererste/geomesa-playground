package org.matsim.contribs.analysis;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.matism.contribs.analysis.GeomesaFileSystemStore;
import org.matism.contribs.analysis.TrajectoryToGeomesaHandler;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventsTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void testSingleAgent() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var store = new GeomesaFileSystemStore(storeRoot);

        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:3857", "EPSG:4326");
        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(ExamplesUtils.getTestScenarioURL("equil").toString() + "network.xml");

        // transform network to wgs84
        network.getNodes().values().parallelStream()
                .forEach(node -> {
                    var coord = transformation.transform(node.getCoord());
                    node.setCoord(coord);
                });

        // parse events file with one agent
        // it is important to wrap the writer in auto-close-try, because it might not flush until it is disposed/closed
        try (var writer = store.getTrajectoryWriter()) {
            var handler = new TrajectoryToGeomesaHandler(writer, network);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(handler);
            new MatsimEventsReader(manager).readFile(testUtils.getInputDirectory() + "output_events.xml.gz");
        }
        var ff = new FilterFactoryImpl();

        // filter by agent id
        var idFilter = ff.equal(ff.property(GeomesaFileSystemStore.TrajectorySchema.AGENT_ID), ff.literal("1"));
        var idResultSet = store.getTrajectoryFeatureCollection(idFilter);

        // the agent in the test set has three legs
        assertEquals(3, idResultSet.size());

        // filter by bounding box slightly south of equator
        var bbox = new GeometryFactory().createPolygon(new Coordinate[]{
                new Coordinate(-10,-0.001),
                new Coordinate(10,-0.001),
                new Coordinate(10,-10),
                new Coordinate(-10,-10),
                new Coordinate(-10,-0.001),
        });
        var bboxFilter = ff.bbox(ff.property(GeomesaFileSystemStore.TrajectorySchema.GEOMETRY), ff.literal(bbox));
        var bboxResultSet = store.getTrajectoryFeatureCollection(bboxFilter);

        assertEquals(1, bboxResultSet.size());

        // filter by time window
        var from = Date.from(Instant.ofEpochSecond(9 * 3600));
        var to = Date.from(Instant.ofEpochSecond(12 * 3600));
        var exitFilter = ff.between(ff.property(GeomesaFileSystemStore.TrajectorySchema.EXIT_TIME), ff.literal(from), ff.literal(to));
        var enterFilter = ff.between(ff.property(GeomesaFileSystemStore.TrajectorySchema.ENTER_TIME), ff.literal(from), ff.literal(to));
        var timeFilter = ff.and(enterFilter, exitFilter);
        var timeResultSet = store.getTrajectoryFeatureCollection(timeFilter);

        assertEquals(1, timeResultSet.size());
    }

    @Test
    public void test2000Agents() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var store = new GeomesaFileSystemStore(storeRoot);

        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:3857", "EPSG:4326");
        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(ExamplesUtils.getTestScenarioURL("equil").toString() + "network.xml");

        // transform network to wgs84
        network.getNodes().values().parallelStream()
                .forEach(node -> {
                    var coord = transformation.transform(node.getCoord());
                    node.setCoord(coord);
                });

        // parse events file with one agent
        // it is important to wrap the writer in auto-close-try, because it might not flush until it is disposed/closed
        try (var writer = store.getTrajectoryWriter()) {
            var handler = new TrajectoryToGeomesaHandler(writer, network);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(handler);
            new MatsimEventsReader(manager).readFile(ExamplesUtils.getTestScenarioURL("equil").toString() + "output_events.xml.gz");
        }
        var ff = new FilterFactoryImpl();

        // check overall legs
        var selectAllResultSet = store.getTrajectoryFeatureCollection(Filter.INCLUDE);
        assertEquals(4000, selectAllResultSet.size());

        // filter by agent id
        // there seems to be a subtle difference between 'equal' and 'equals'
        var idFilter = ff.equals(ff.property(GeomesaFileSystemStore.TrajectorySchema.AGENT_ID), ff.literal("1"));
        var idResultSet = store.getTrajectoryFeatureCollection(idFilter);

        // the agent in the test set has three legs
        assertEquals(2, idResultSet.size());

        // filter by bounding box slightly south of equator
        var bbox = new GeometryFactory().createPolygon(new Coordinate[]{
                new Coordinate(-10,-0.001),
                new Coordinate(10,-0.001),
                new Coordinate(10,-10),
                new Coordinate(-10,-10),
                new Coordinate(-10,-0.001),
        });
        var bboxFilter = ff.bbox(ff.property(GeomesaFileSystemStore.TrajectorySchema.GEOMETRY), ff.literal(bbox));
        var bboxResultSet = store.getTrajectoryFeatureCollection(bboxFilter);

        assertEquals(2000, bboxResultSet.size());

        // filter by time window
        var from = Date.from(Instant.ofEpochSecond(13 * 3600));
        var to = Date.from(Instant.ofEpochSecond(16 * 3600));
        var exitFilter = ff.between(ff.property(GeomesaFileSystemStore.TrajectorySchema.EXIT_TIME), ff.literal(from), ff.literal(to));
        var enterFilter = ff.between(ff.property(GeomesaFileSystemStore.TrajectorySchema.ENTER_TIME), ff.literal(from), ff.literal(to));
        var timeFilter = ff.and(enterFilter, exitFilter);
        var timeResultSet = store.getTrajectoryFeatureCollection(timeFilter);

        // some arbitrary number of legs between 2pm and 5pm
        assertEquals(236, timeResultSet.size());
    }

    @Test
    public void testConfigUtils() {

        var config = ConfigUtils.loadConfig(ExamplesUtils.getTestScenarioURL("equil").toString() + "config.xml");
        assertNotNull(config);
    }
}
