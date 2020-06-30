package org.matsim.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.log4j.Log4j2;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.filter.FilterFactoryImpl;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Log4j2
public class EventsToGeomesa2 {

    @Parameter(names = "-e", required = true)
    private final String events = "";

    @Parameter(names = "-n", required = true)
    private final String networkFile = "";

    @Parameter(names = "-store", required = true)
    private final String storeRoot = "";

    // this could be somewhat dynamic later on as well
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:3857", "EPSG:4326");

    public static void main(String[] args) throws IOException {

        var converter = new EventsToGeomesa2();
        JCommander.newBuilder().addObject(converter).build().parse(args);
       // converter.convert();
        converter.read();
    }

    private void convert() throws IOException {

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        var schemaType = TrajectoryFeatureType.createFeatureType();
        store.createSchema(schemaType);

        log.info("Read in network");
        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        // project network onto EPSG:4326
        log.info("Transform network");
        for (Node node : network.getNodes().values()) {
            var transformedCoord = transformation.transform(node.getCoord());
            node.setCoord(transformedCoord);
        }

        var handler = new TrajectoryToGeomesaHandler(store.getFeatureWriterAppend(schemaType.getTypeName(), Transaction.AUTO_COMMIT), network);
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);
        log.info("starting to read events file");
        new MatsimEventsReader(manager).readFile(events);

        log.info("Done reading events");
        store.dispose();
    }

    private void read() throws IOException {

        var factory = new FilterFactoryImpl();
        Date from = Date.from(Instant.ofEpochSecond(0));
        Date to = Date.from(Instant.ofEpochSecond(3600 * 10));

        // note: BETWEEN is inclusive, while DURING is exclusive

        Filter dateFilter = factory.between(factory.property(TrajectoryFeatureType.exitTime), factory.literal(from), factory.literal(to));
        var query = new Query(TrajectoryFeatureType.createFeatureType().getTypeName(), dateFilter);
        //Query query = new Query(TrajectoryFeatureType.createFeatureType().getTypeName(), Filter.INCLUDE);

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        try (var reader = store.getFeatureReader(query, Transaction.AUTO_COMMIT)) {

            while(reader.hasNext()) {
                var feature = reader.next();

                StringBuilder message = new StringBuilder(feature.getID() + ": ");
                for (Object attribute : feature.getAttributes()) {

                    var attrString = attribute == null ? "null" : attribute.toString();
                    message.append(attrString).append(", ");
                }
                log.info(message);
            }
        }
    }
}