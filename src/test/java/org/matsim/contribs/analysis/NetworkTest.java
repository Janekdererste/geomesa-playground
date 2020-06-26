package org.matsim.contribs.analysis;

import lombok.extern.log4j.Log4j2;
import org.geotools.data.Query;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.matism.contribs.analysis.GeomesaFileSystemStore;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Log4j2
public class NetworkTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void readInEquilNetwork() throws IOException, CQLException {

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

        try (var writer = store.getNetworkWriter()) {
            for (var link : network.getLinks().values()) {

                var lineString = "LINESTRING ("
                        + link.getFromNode().getCoord().getX() + " "
                        + link.getFromNode().getCoord().getY() + ","
                        + link.getToNode().getCoord().getX() + " "
                        + link.getToNode().getCoord().getY() + ")";

                var toWrite = writer.next();
                toWrite.setAttribute(GeomesaFileSystemStore.NetworkSchema.GEOMETRY, lineString);
                toWrite.setAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID, link.getId());

                writer.write();
            }
        }

        // try index of link ids
        var idFilter = ECQL.toFilter(GeomesaFileSystemStore.NetworkSchema.LINK_ID + " = 1");
        try (var reader = store.getNetworkReader(idFilter)) {
            while (reader.hasNext()) {
                var feature = reader.next();
                assertEquals("1", feature.getAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID));
            }
        }

        // try spatial filter
        var bbox = new GeometryFactory().createPolygon(new Coordinate[]{
                new Coordinate(0,-10),
                new Coordinate(10,-10),
                new Coordinate(10,10),
                new Coordinate(0,10),
                new Coordinate(0,-10),
        });

        FilterFactory2 filterFactory = new FilterFactoryImpl();
        var boxFilter = filterFactory.within(
                filterFactory.property(GeomesaFileSystemStore.NetworkSchema.GEOMETRY), filterFactory.literal(bbox));
        try(var reader = store.getNetworkReader(boxFilter)) {
            while(reader.hasNext()) {
                var feature = reader.next();
                if (!feature.getAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID).equals("21") && !feature.getAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID).equals("20")) {
                    fail("expecting only two links with ids 20 and 21 to have positive x-coordinates");
                }
            }
        }
    }
}
