package org.matism.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.log4j.Log4j2;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.locationtech.geomesa.fs.data.FileSystemDataStore;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

import javax.print.DocFlavor;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class NetworkToGeomesa {

    private static final GeometryFactory geometryFactory = new GeometryFactory();
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:25832", "EPSG:4326");

    @Parameter(names = "-network")
    private String networkFile = "";

    @Parameter(names = "-storeRoot")
    private String storeRoot = "";

    public static void main(String[] args) throws IOException, CQLException {

        var converter = new NetworkToGeomesa();
        JCommander.newBuilder().addObject(converter).build().parse(args);
        converter.convert();
    }

    private void convert() throws IOException, CQLException {

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        var schemaType = createSchema();
        store.createSchema(schemaType);

        log.info("starting to read network");
        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        log.info("writing network into store");
        try(var writer = store.getFeatureWriterAppend(schemaType.getTypeName(), Transaction.AUTO_COMMIT)) {
            for(var link : network.getLinks().values()) {

                var toWrite = writer.next();

                var geometry = createGeometry(link);

                toWrite.setAttribute("geom", geometry);
                toWrite.setAttribute("linkId", link.getId().toString());
                toWrite.setAttribute("fromNodeId", link.getFromNode().getId().toString());
                toWrite.setAttribute("toNodeId", link.getToNode().getId().toString());
                toWrite.setAttribute("capacity", link.getCapacity());
                toWrite.setAttribute("freespeed", link.getFreespeed());
                toWrite.setAttribute("length", link.getLength());
                toWrite.setAttribute("numberOfLanes", link.getNumberOfLanes());

                writer.write();
            }
        }

        // now do a test query: taken from ruhr filter thingy
        var bottomLeft = transformation.transform(new Coord(317373, 5675521.));
        var topRight = transformation.transform(new Coord(418575., 5736671.));

        FilterFactory2 factory = new FilterFactoryImpl();
        var filter = factory.bbox(factory.property("geom"), bottomLeft.getX(), bottomLeft.getY(), topRight.getX(), topRight.getY(), "");

        var q1 = new Query(schemaType.getTypeName(), filter);
        var query = new Query(schemaType.getTypeName(),
                ECQL.toFilter("bbox(geom," +
                        bottomLeft.getX() + ","
                        + bottomLeft.getY() + ","
                        + topRight.getX() + ","
                        + topRight.getY() + ")"));

        log.info("start dumping links");
        List<String> bla = new ArrayList<>();
        try (var reader = store.getFeatureReader(query, Transaction.AUTO_COMMIT)) {
            while(reader.hasNext()) {
                var feature = reader.next();

                StringBuilder message = new StringBuilder(feature.getID() + ": ");
                for (Object attribute : feature.getAttributes()) {
                    message.append(attribute.toString()).append(", ");
                }
                bla.add(message.toString());
            }
        }

        log.info("fetched: " + bla.size() + " links");

        store.dispose();
    }

    private SimpleFeatureType createSchema() {

        String schema = "*geom:LineString:srid=4326,"
                + "capacity:Double,"
                + "freespeed:Double,"
                + "length:Double,"
                + "numberOfLanes:Double,"
                + "linkId:String,"
                + "fromNodeId:String,"
                + "toNodeId:String";

        var schemaType = SimpleFeatureTypes.createType("network", schema);
        ConfigurationUtils.setScheme(schemaType, "xz2-10bit", Map.of());
        return schemaType;
    }

    private Geometry createGeometry(Link link) {

        var fromCoord = transformation.transform(link.getFromNode().getCoord());
        var toCoord = transformation.transform(link.getToNode().getCoord());

        return geometryFactory.createLineString(new Coordinate[]{
                MGC.coord2Coordinate(fromCoord), MGC.coord2Coordinate(toCoord)
        });
    }
}
