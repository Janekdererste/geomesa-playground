package org.matism.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.log4j.Log4j2;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.filter.FilterFactoryImpl;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.geotools.SchemaBuilder;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Log4j2
public class EventsToGeomesa2 {

    @Parameter(names = "-e", required = true)
    private String events = "";

    @Parameter(names = "-n", required = true)
    private String networkFile = "";

    @Parameter(names = "-store", required = true)
    private String storeRoot = "";

    // this could be somewhat dynamic later on as well
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:25832", "EPSG:4326");

    public static void main(String[] args) throws IOException {

        var converter = new EventsToGeomesa2();
        JCommander.newBuilder().addObject(converter).build().parse(args);
        converter.convert();
        converter.read();
    }

    private void convert() throws IOException {

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        var schemaType = createSchema();
        store.createSchema(schemaType);

        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        var handler = new GeomesaHandler(store.getFeatureWriterAppend(schemaType.getTypeName(), Transaction.AUTO_COMMIT), network, transformation);
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);
        log.info("starting to read events file");
        new MatsimEventsReader(manager).readFile(events);

        store.dispose();
    }

    private void read() throws IOException {

        var factory = new FilterFactoryImpl();
        Date from = Date.from(Instant.ofEpochSecond(3600 * 8));
        Date to = Date.from(Instant.ofEpochSecond(3600 * 10));

        // note: BETWEEN is inclusive, while DURING is exclusive
        Filter dateFilter = factory.between(factory.property("time"), factory.literal(from), factory.literal(to));
        Query query = new Query(createSchema().getTypeName(), dateFilter);

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        try (var reader = store.getFeatureReader(query, Transaction.AUTO_COMMIT)) {

            while(reader.hasNext()) {
                var feature = reader.next();

                StringBuilder message = new StringBuilder(feature.getID() + ": ");
                for (Object attribute : feature.getAttributes()) {
                    message.append(attribute.toString()).append(", ");
                }
                log.info(message);
            }
        }
    }

    private SimpleFeatureType createSchema() {

        var schema = SchemaBuilder.builder()
                .addDate("time", true).end() // I guess the boolean means 'isIndex'
                .addPoint("geometry", true).end()
                .addString("type").withIndex().end()
                //.addString("vehicleId").withIndex().end()
                .build("linkEvents");

        // tell the file storage to partition the data into minutes and use 16bit geohashes
        ConfigurationUtils.setScheme(schema, "minute,z2-16bit", Collections.emptyMap());
        return schema;
    }
}
